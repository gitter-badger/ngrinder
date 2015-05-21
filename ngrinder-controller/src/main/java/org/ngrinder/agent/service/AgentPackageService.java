package org.ngrinder.agent.service;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.util.CompressionUtils.FilePredicate;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.CompressionUtils.*;
import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Agent package service.
 *
 * @author Matt
 * @since 3.3
 */
@Service
public class AgentPackageService {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AgentPackageService.class);
	public static final int EXEC = 0x81ed;
	private static final int TIME_MILLIS_OF_DAY = 1000 * 60 * 60 * 24;

	@Autowired
	private Config config;

	@Autowired
	private ScheduledTaskService scheduledTaskService;

	@PostConstruct
	public void init() {
		// clean up package directories not to occupy too much spaces.
		cleanUpPackageDir(true);
		scheduledTaskService.addFixedDelayedScheduledTask(new Runnable() {
			@Override
			public void run() {
				cleanUpPackageDir(false);
			}
		}, TIME_MILLIS_OF_DAY);
	}

	private void cleanUpPackageDir(boolean all) {
		synchronized (this) {
			final File packagesDir = getPackagesDir();
			final File[] files = packagesDir.listFiles();
			if (files != null) {
				for (File each : files) {
					if (!each.isDirectory()) {
						long expiryTimestamp = each.lastModified() + (TIME_MILLIS_OF_DAY * 2);
						if (all || expiryTimestamp < System.currentTimeMillis()) {
							FileUtils.deleteQuietly(each);
						}
					}
				}
			}
		}
	}

	/**
	 * Get package name
	 *
	 * @param moduleName nGrinder module name.
	 * @return String module full name.
	 */
	public String getPackageName(String moduleName) {
		return moduleName + "-" + config.getVersion();
	}

	/**
	 * Get distributable package name with appropriate extension.
	 *
	 * @param moduleName   nGrinder sub  module name.
	 * @param regionName   region   name
	 * @param connectionIP where it will connect to
	 * @param ownerName    owner name
	 * @param forWindow    if true, then package type is zip,if false, package type is tar.
	 * @return String  module full name.
	 */
	public String getDistributionPackageName(PackageSetting setting, boolean forWindow) {
		return getPackageName(setting.moduleName) + getFilenameComponent(setting.regionName) + 
				getFilenameComponent(setting.connectionIP) + 
				getFilenameComponent(setting.owner) + (forWindow ? ".zip" : ".tar");
	}

	private String getFilenameComponent(String value) {
		value = trimToEmpty(value);
		if (isNotEmpty(value)) {
			value = "-" + value;
		}
		return value;
	}

	/**
	 * Get the agent package containing folder.
	 *
	 * @return File  agent package dir.
	 */
	public File getPackagesDir() {
		return config.getHome().getSubFile("download");
	}

	/**
	 * Create agent package.
	 *
	 * @return File  agent package.
	 */
	public synchronized File createAgentPackage() {
		return createAgentPackage(null, null, config.getControllerPort(), null);
	}

	/**
	 * Create agent package.
	 *
	 * @param connectionIP host ip.
	 * @param region       region
	 * @param owner        owner
	 * @return File  agent package.
	 */
	public synchronized File createAgentPackage(String region, String connectionIP, int port, String owner) {
		return createAgentPackage((URLClassLoader) getClass().getClassLoader(), region, connectionIP, port, owner);
	}
	
	public File createPackage(PackageSetting setting) {
		File tarPackage = preparePackage(setting);
		
		if (tarPackage.exists()) {
			return tarPackage;
		}
		
		final String basePath = setting.moduleName + "/";
		final String libPath = basePath + "lib/";
		TarArchiveOutputStream tarOutputStream = null;
		
		try {
			tarOutputStream = packagingToTar(setting, tarPackage, basePath, libPath);
		} catch (IOException e) {
			LOGGER.error("Error while generating an " + setting.moduleName + e.getMessage());
		} finally {
			IOUtils.closeQuietly(tarOutputStream);
		}
		
		return tarPackage;
	}

	private TarArchiveOutputStream packagingToTar(final PackageSetting setting,
			File tarPackage, final String basePath, final String libPath) throws IOException {		
		TarArchiveOutputStream tarOutputStream;
		tarOutputStream = createTarArchiveStream(tarPackage);
		addFolderToTar(tarOutputStream, basePath);
		addFolderToTar(tarOutputStream, libPath);
		
		for (URL eachUrl : setting.urlClassLoader.getURLs()) {
			File eachClassPath = new File(eachUrl.getFile());
			if (!isJar(eachClassPath)) {
				continue;
			}
			if (isAgentDependentLib(eachClassPath, "ngrinder-sh")) {
				processJarEntries(eachClassPath, new TarArchivingZipEntryProcessor(tarOutputStream, new FilePredicate() {
					@Override
					public boolean evaluate(Object object) {
						ZipEntry zipEntry = (ZipEntry) object;
						return setting.isDependentExec(zipEntry.getName());
					}
				}, basePath, EXEC));
			} else if (setting.isDependentLibs(eachClassPath)) {
				addFileToTar(tarOutputStream, eachClassPath, libPath + eachClassPath.getName());
			}
		}
		setting.addConfToTar(tarOutputStream, basePath, setting.regionName, setting.connectionIP,setting.port, setting.owner);
		return tarOutputStream;
	}

	private File preparePackage(PackageSetting setting) {
		File packageDir = getPackagesDir();
		if (packageDir.mkdirs()) {
			LOGGER.info("{} is created", packageDir.getPath());
		}
		final String packageName = getDistributionPackageName(setting, false);
		File tarPackage = new File(packageDir, packageName);
		return tarPackage;
	}

	public File createMonitorPackage() {		
		final URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
		
		return createPackage(new PackageSetting(classLoader, "ngrinder-monitor", null, null, config.getMonitorPort(), null) {
			@Override
			public boolean isDependentLibs(File file) throws IOException {
				return isMonitorDependentLib(file, getMonitorDependentLibs(classLoader));
			}
			
			@Override
			public boolean isDependentExec(String filename) {
				return filename.contains("monitor") && (filename.endsWith("sh") || filename.endsWith("bat"));
			}
			
			@Override
			public void addConfToTar(TarArchiveOutputStream tarOutputStream, String basePath, String regionName, String connectionIP, int port, String owner) throws IOException {
				addMonitorConfToTar(tarOutputStream, basePath, port);
			}
		});
	}

	/**
	 * Create agent package
	 *
	 * @param classLoader  URLClass Loader
	 * @param regionName   region
	 * @param connectionIP host ip
	 * @param port         host port
	 * @param owner        owner
	 * @return File
	 */
	synchronized File createAgentPackage(final URLClassLoader classLoader, String regionName, String connectionIP,
	                                     int port, String owner) {
		return createPackage(new PackageSetting(classLoader, "ngrinder-agent", regionName, connectionIP, port, owner) {
			@Override
			public boolean isDependentLibs(File file) throws IOException {
				return isAgentDependentLib(file, getDependentLibs(classLoader));
			}
			
			@Override
			public boolean isDependentExec(String filename) {
				return filename.contains("agent") && (filename.endsWith("sh") || filename.endsWith("bat"));
			}
			
			@Override
			public void addConfToTar(TarArchiveOutputStream tarOutputStream, String basePath, String regionName, String connectionIP, int port, String owner) throws IOException {
				addAgentConfToTar(tarOutputStream, basePath, regionName, connectionIP, port, owner);
			}
		});
	}

	/**
	 * Create sitemonitor package
	 *
	 * @param connectionIP host ip
	 * @param port         host port
	 * @param owner        owner
	 * @return File
	 */
	public synchronized File createSitemonitorPackage(final URLClassLoader classLoader, String connectionIP, int port, String owner) {
		return createPackage(new PackageSetting(classLoader, "ngrinder-sitemonitor", null, connectionIP, port, owner) {
			@Override
			public boolean isDependentLibs(File file) throws IOException {
				return isAgentDependentLib(file, getDependentLibs(classLoader));
			}
			
			@Override
			public boolean isDependentExec(String filename) {
				return filename.contains("sitemonitor") && (filename.endsWith("sh") || filename.endsWith("bat"));
			}
			
			@Override
			public void addConfToTar(TarArchiveOutputStream tarOutputStream, String basePath, String regionName, String connectionIP, int port, String owner) throws IOException {
				addSitemonitorConfToTar(tarOutputStream, basePath, connectionIP, port, owner);
			}
		});
	}

	private TarArchiveOutputStream createTarArchiveStream(File agentTar) throws IOException {
		FileOutputStream fos = new FileOutputStream(agentTar);
		return new TarArchiveOutputStream(new BufferedOutputStream(fos));
	}

	private void addMonitorConfToTar(TarArchiveOutputStream tarOutputStream, String basePath,
	                                 Integer monitorPort) throws IOException {
		final String config = getAgentConfigContent("agent_monitor.conf", buildMap("monitorPort",
				(Object) String.valueOf(monitorPort)));
		final byte[] bytes = config.getBytes();
		addInputStreamToTar(tarOutputStream, new ByteArrayInputStream(bytes), basePath + "__agent.conf",
				bytes.length, TarArchiveEntry.DEFAULT_FILE_MODE);
	}

	private void addAgentConfToTar(TarArchiveOutputStream tarOutputStream, String basePath,
	                               String regionName, String connectingIP,
	                               int port, String owner) throws IOException {
		if (isNotEmpty(connectingIP)) {
			final String config = getAgentConfigContent("agent_agent.conf", getAgentConfigParam(regionName,
					connectingIP, port, owner));
			final byte[] bytes = config.getBytes();
			addInputStreamToTar(tarOutputStream, new ByteArrayInputStream(bytes), basePath + "__agent.conf",
					bytes.length, TarArchiveEntry.DEFAULT_FILE_MODE);
		}
	}

	private void addSitemonitorConfToTar(TarArchiveOutputStream tarOutputStream, String basePath,
	                               String connectingIP, int port, String owner) throws IOException {
		if (isNotEmpty(connectingIP)) {
			final String config = getAgentConfigContent("agent_sitemonitor.conf", getSitemonitorConfigParam(
					connectingIP, port, owner));
			final byte[] bytes = config.getBytes();
			addInputStreamToTar(tarOutputStream, new ByteArrayInputStream(bytes), basePath + "__agent.conf",
					bytes.length, TarArchiveEntry.DEFAULT_FILE_MODE);
		}
	}

	private Set<String> getMonitorDependentLibs(URLClassLoader cl) throws IOException {
		Set<String> libs = new HashSet<String>();
		InputStream dependencyStream = null;
		try {
			dependencyStream = cl.getResourceAsStream("monitor-dependencies.txt");
			final String dependencies = IOUtils.toString(dependencyStream);
			for (String each : StringUtils.split(dependencies, ";")) {
				libs.add(FilenameUtils.getBaseName(each.trim()).replace("-SNAPSHOT", ""));
			}
		} catch (Exception e) {
			IOUtils.closeQuietly(dependencyStream);
			LOGGER.error("Error while loading monitor-dependencies.txt", e);
		}

		return libs;
	}

	private Set<String> getDependentLibs(URLClassLoader cl) throws IOException {
		Set<String> libs = new HashSet<String>();
		InputStream dependencyStream = null;
		try {
			dependencyStream = cl.getResourceAsStream("dependencies.txt");
			final String dependencies = IOUtils.toString(dependencyStream);
			for (String each : StringUtils.split(dependencies, ";")) {
				libs.add(FilenameUtils.getBaseName(each.trim()).replace("-SNAPSHOT", ""));
			}
			libs.add(getPackageName("ngrinder-core").replace("-SNAPSHOT", ""));
			libs.add(getPackageName("ngrinder-runtime").replace("-SNAPSHOT", ""));
			libs.add(getPackageName("ngrinder-groovy").replace("-SNAPSHOT", ""));
		} catch (Exception e) {
			IOUtils.closeQuietly(dependencyStream);
			LOGGER.error("Error while loading dependencies.txt", e);
		}

		return libs;
	}

	private Map<String, Object> getAgentConfigParam(String regionName, String controllerIP, int port, String owner) {
		Map<String, Object> confMap = newHashMap();
		confMap.put("controllerIP", controllerIP);
		confMap.put("controllerPort", String.valueOf(port));
		if (StringUtils.isEmpty(regionName)) {
			regionName = "NONE";
		}
		if (StringUtils.isNotBlank(owner)) {
			if (StringUtils.isEmpty(regionName)) {
				regionName = "owned_" + owner;
			} else {
				regionName = regionName + "_owned_" + owner;
			}
		}
		confMap.put("controllerRegion", regionName);
		return confMap;
	}

	private Map<String, Object> getSitemonitorConfigParam(String controllerIP, int port, String owner) {
		Map<String, Object> confMap = newHashMap();
		confMap.put("controllerIP", controllerIP);
		confMap.put("controllerPort", String.valueOf(port));
		if (owner != null) {
			confMap.put("owner", owner);
		}
		
		return confMap;
	}

	/**
	 * Check if this given path is jar.
	 *
	 * @param libFile lib file
	 * @return true if it's jar
	 */
	public boolean isJar(File libFile) {
		return StringUtils.endsWith(libFile.getName(), ".jar");
	}

	/**
	 * Check if this given lib file is the given library.
	 *
	 * @param libFile lib file
	 * @param libName desirable name
	 * @return true if dependent lib
	 */
	public boolean isAgentDependentLib(File libFile, String libName) {
		return StringUtils.startsWith(libFile.getName(), libName);
	}

	/**
	 * Check if this given lib file in the given lib set.
	 *
	 * @param libFile lib file
	 * @param libs    lib set
	 * @return true if dependent lib
	 */
	public boolean isMonitorDependentLib(File libFile, Set<String> libs) {
		if (libFile.getName().contains("grinder-3.9.1.jar")) {
			return false;
		}
		String name = libFile.getName();
		name = name.replace(".jar", "").replace("-SNAPSHOT", "");
		for (String each : libs) {
			if (name.contains(each)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if this given lib file in the given lib set.
	 *
	 * @param libFile lib file
	 * @param libs    lib set
	 * @return true if dependent lib
	 */
	public boolean isAgentDependentLib(File libFile, Set<String> libs) {
		if (libFile.getName().contains("grinder-3.9.1.jar")) {
			return false;
		}
		String name = libFile.getName();
		name = name.replace(".jar", "").replace("-SNAPSHOT", "");
		return libs.contains(name);
	}

	/**
	 * Get the agent.config content replacing the variables with the given values.
	 *
	 * @param templateName template name.
	 * @param values       map of configurations.
	 * @return generated string
	 */
	public String getAgentConfigContent(String templateName, Map<String, Object> values) {
		StringWriter writer = null;
		try {
			Configuration config = new Configuration();
			ClassPathResource cpr = new ClassPathResource("ngrinder_agent_home_template");
			config.setDirectoryForTemplateLoading(cpr.getFile());
			config.setObjectWrapper(new DefaultObjectWrapper());
			Template template = config.getTemplate(templateName);
			writer = new StringWriter();
			template.process(values, writer);
			return writer.toString();
		} catch (Exception e) {
			throw processException("Error while fetching the script template.", e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	static class TarArchivingZipEntryProcessor implements ZipEntryProcessor {
		private TarArchiveOutputStream tao;
		private FilePredicate filePredicate;
		private String basePath;
		private int mode;

		TarArchivingZipEntryProcessor(TarArchiveOutputStream tao, FilePredicate filePredicate, String basePath, int mode) {
			this.tao = tao;
			this.filePredicate = filePredicate;
			this.basePath = basePath;
			this.mode = mode;
		}

		@Override
		public void process(ZipFile file, ZipEntry entry) throws IOException {
			InputStream inputStream = null;
			try {
				inputStream = file.getInputStream(entry);

				if (filePredicate.evaluate(entry)) {
					addInputStreamToTar(this.tao, inputStream, basePath + FilenameUtils.getName(entry.getName()),
							entry.getSize(),
							this.mode);
				}
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
	}
	
	abstract class PackageSetting {
		URLClassLoader urlClassLoader;
		String moduleName;
		String regionName;
		String connectionIP;
		Integer port;
		String owner;
		
		public PackageSetting(URLClassLoader urlClassLoader, String moduleName, String regionName, 
				String connectionIP, Integer port, String owner) {
			this.urlClassLoader = urlClassLoader;
			this.moduleName = moduleName;
			this.regionName = regionName;
			this.connectionIP = connectionIP;
			this.port = port;
			this.owner = owner;
		}
		abstract public boolean isDependentLibs(File file) throws IOException;
		abstract public boolean isDependentExec(String filename);
		abstract public void addConfToTar(TarArchiveOutputStream tarOutputStream, String basePath,
			 String regionName, String connectionIP,
			 int port, String owner) throws IOException;
	}

}
