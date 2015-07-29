/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder;

import com.beust.jcommander.JCommander;
import net.grinder.AgentControllerDaemon;
import net.grinder.engine.agent.SiteMonScriptRunner;
import net.grinder.util.VersionNumber;
import net.grinder.util.thread.Condition;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.ngrinder.common.constants.AgentConstants;
import org.ngrinder.common.constants.CommonConstants;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.ArchLoaderInit;
import org.ngrinder.monitor.agent.MonitorServer;
import org.ngrinder.sitemon.MonitorScheduler;
import org.ngrinder.sitemon.MonitorSchedulerImplementation;
import org.ngrinder.sitemon.SiteMonController;
import org.ngrinder.sitemon.SiteMonControllerDaemon;
import org.ngrinder.util.AgentStateMonitor;
import org.ngrinder.util.AgentStateMonitorImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static net.grinder.util.NetworkUtils.getIP;
import static org.ngrinder.common.constants.InternalConstants.PROP_INTERNAL_NGRINDER_VERSION;
import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Main class to start agent or monitor.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 * @author Gisoo Gwon
 * @since 3.0
 */
public class NGrinderAgentStarter implements AgentConstants, CommonConstants {

	private static final Logger LOG = LoggerFactory.getLogger("starter");
	
	// folder for script file download
	public static final String SITEMON_ROOT_DIR = "sitemon-file";
	public static final String FILESTORE_SUB_DIR = "incoming";

	private AgentConfig agentConfig;

	private AgentControllerDaemon agentController;


	/**
	 * Constructor.
	 */
	public NGrinderAgentStarter() {
	}

	public void init() {
		// Check agent start mode
		this.agentConfig = createAgentConfig();
		try {
			new ArchLoaderInit().init(agentConfig.getHome().getNativeDirectory());
		} catch (Exception e) {
			LOG.error("Error while expanding native lib", e);
		}
	}

	protected AgentConfig createAgentConfig() {
		AgentConfig agentConfig = new AgentConfig();
		agentConfig.init();
		return agentConfig;
	}

	/*
	 * Get the start mode, "agent" or "monitor". If it is not set in configuration, it will return "agent".
	 */
	public String getStartMode() {
		return agentConfig.getCommonProperties().getProperty(PROP_COMMON_START_MODE);
	}

	/**
	 * Get agent version.
	 *
	 * @return version string
	 */
	public String getVersion() {
		return agentConfig.getInternalProperties().getProperty(PROP_INTERNAL_NGRINDER_VERSION);
	}

	/**
	 * Start the performance monitor.
	 */
	public void startMonitor() {
		printLog("***************************************************");
		printLog("* Start nGrinder Monitor... ");
		printLog("***************************************************");
		try {
			MonitorServer.getInstance().init(agentConfig);
			MonitorServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			printHelpAndExit("Error while starting Monitor", e);
		}
	}

	/**
	 * Stop monitors.
	 * Only for unit-test.
	 */
	void stopMonitor() {
		MonitorServer.getInstance().stop();
	}

	/**
	 * Start ngrinder agent.
	 */
	public void startAgent() {
		printLog("***************************************************");
		printLog("   Start nGrinder Agent ...");
		printLog("***************************************************");

		if (StringUtils.isEmpty(System.getenv("JAVA_HOME"))) {
			printLog("Hey!! JAVA_HOME env var was not provided. "
					+ "Please provide JAVA_HOME env var before running agent."
					+ "Otherwise you can not execute the agent in the security mode.");
		}


		boolean serverMode = agentConfig.isServerMode();
		if (!serverMode) {
			printLog("JVM server mode is disabled.");
		}

		String controllerIP = getIP(agentConfig.getControllerIP());
		int controllerPort = agentConfig.getControllerPort();
		agentConfig.setControllerHost(controllerIP);
		LOG.info("connecting to controller {}:{}", controllerIP, controllerPort);

		try {
			agentController = new AgentControllerDaemon(agentConfig);
			agentController.run();
		} catch (Exception e) {
			LOG.error("Error while connecting to : {}:{}", controllerIP, controllerPort);
			printHelpAndExit("Error while starting Agent", e);
		}

	}

	private void printLog(String s, Object... args) {
		if (!agentConfig.isSilentMode()) {
			LOG.info(s, args);
		}
	}

	/**
	 * Stop the ngrinder agent.
	 * Only for unit-test.
	 */
	void stopAgent() {
		LOG.info("Stop nGrinder agent!");
		agentController.shutdown();
	}

	/**
	 * Start the sitemon agent.
	 */
	public void startSiteMonAgent() {
		printLog("***************************************************");
		printLog("* Start nGrinder sitemon agent... ");
		printLog("***************************************************");
		
		try {
			String controllerIP = getIP(agentConfig.getSiteMonAgentControllerIp());
			int controllerPort = agentConfig.getSiteMonAgentControllerPort();
			String owner = agentConfig.getSiteMonAgentOwner();
			LOG.info("connecting to controller {}, owner-{}", (controllerIP + ":" + controllerPort), owner);

			try {
				File agentRootDir = agentConfig.getHome().getDirectory();
				File siteMonDir = new File(agentRootDir, SITEMON_ROOT_DIR);
				File scriptDistDir = new File(siteMonDir, FILESTORE_SUB_DIR);
				AgentStateMonitor agentStateMonitor = new AgentStateMonitorImplementation(agentConfig);
				
				if (!siteMonDir.exists()) {
					siteMonDir.mkdir();
				}

				Condition eventSyncCondition = new Condition();
				// init for script runner process
				SiteMonScriptRunner scriptRunner
					= new SiteMonScriptRunner(scriptDistDir);
				MonitorScheduler monitorScheduler
					= new MonitorSchedulerImplementation(scriptRunner, agentStateMonitor);
				// init for agent main process
				SiteMonController controller = new SiteMonController(
					siteMonDir, agentConfig, eventSyncCondition);
				controller.setMonitorScheduler(monitorScheduler);
				controller.setAgentStateMonitor(agentStateMonitor);
				SiteMonControllerDaemon daemon = new SiteMonControllerDaemon(controller);
				// start agent process
				daemon.run();
			} catch (Exception e) {
				LOG.error("Error while connecting to : {}:{}", controllerIP, controllerPort);
				printHelpAndExit("Error while starting Agent", e);
			}
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			printHelpAndExit("Error while starting sitemon agent", e);
		}
	}

	public static NGrinderAgentStarterParam.NGrinderModeParam modeParam;

	/**
	 * Agent starter.
	 *
	 * @param args arguments
	 */
	public static void main(String[] args) {
		NGrinderAgentStarter starter = new NGrinderAgentStarter();
		final NGrinderAgentStarterParam param = new NGrinderAgentStarterParam();
		checkJavaVersion();
		JCommander commander = new JCommander(param);
		commander.setProgramName("ngrinder-agent");
		commander.setAcceptUnknownOptions(true);
		try {
			commander.parse(args);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return;
		}
		final List<String> unknownOptions = commander.getUnknownOptions();
		modeParam = param.getModeParam();
		modeParam.parse(unknownOptions.toArray(new String[unknownOptions.size()]));

		if (modeParam.version != null) {
			System.out.println("nGrinder v" + getStaticVersion());
			return;
		}

		if (modeParam.help != null) {
			modeParam.usage();
			return;
		}

		System.getProperties().putAll(modeParam.params);
		starter.init();

		final String startMode = modeParam.name();
		if ("stop".equalsIgnoreCase(param.command)) {
			starter.stopProcess(startMode);
			System.out.println("Stop the " + startMode);
			return;
		}
		starter.checkDuplicatedRun(startMode);
		if (startMode.equalsIgnoreCase("agent")) {
			starter.startAgent();
		} else if (startMode.equalsIgnoreCase("monitor")) {
			starter.startMonitor();
		} else if (startMode.equalsIgnoreCase("sitemon")) {
			starter.startSiteMonAgent();
		} else {
			staticPrintHelpAndExit("Invalid agent.conf, '--mode' must be set as 'monitor' or 'agent' or 'sitemon'.");
		}
	}

	private static String getStaticVersion() {
		InputStream inputStream = null;
		Properties properties = new Properties();
		try {
			inputStream = NGrinderAgentStarter.class.getResourceAsStream("/internal.properties");
			properties.load(inputStream);
		} catch (IOException e) {
			// Do nothing.
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return properties.getProperty("ngrinder.version", "UNKNOWN");
	}

	static void checkJavaVersion() {
		String curJavaVersion = System.getProperty("java.version", "1.6");
		checkJavaVersion(curJavaVersion);
	}

	static void checkJavaVersion(String curJavaVersion) {
		if (new VersionNumber(curJavaVersion).compareTo(new VersionNumber("1.6")) < 0) {
			LOG.info("- Current java version {} is less than 1.6. nGrinder Agent might not work well", curJavaVersion);
		}
	}

	/**
	 * Stop process.
	 *
	 * @param mode agent or monitor or sitemon.
	 */
	protected void stopProcess(String mode) {
		String pid = agentConfig.getAgentPidProperties(mode);
		try {
			if (StringUtils.isNotBlank(pid)) {
				new Sigar().kill(pid, 15);
			}
			agentConfig.updateAgentPidProperties(mode);
		} catch (SigarException e) {
			printHelpAndExit(String.format("Error occurred while terminating %s process.\n"
					+ "It can be already stopped or you may not have the permission.\n"
					+ "If everything is OK. Please stop it manually.", mode), e);
		}
	}

	/**
	 * Check if the process is already running in this env.
	 *
	 * @param startMode monitor or agent
	 */
	public void checkDuplicatedRun(String startMode) {
		Sigar sigar = new Sigar();
		String existingPid = this.agentConfig.getAgentPidProperties(startMode);
		if (StringUtils.isNotEmpty(existingPid)) {
			try {
				ProcState procState = sigar.getProcState(existingPid);
				if (procState.getState() == ProcState.RUN || procState.getState() == ProcState.IDLE
						|| procState.getState() == ProcState.SLEEP) {
					printHelpAndExit("Currently " + startMode + " is running with pid " + existingPid
							+ ". Please stop it before run");
				}
				agentConfig.updateAgentPidProperties(startMode);
			} catch (SigarException e) {
				noOp();
			}
		}
		this.agentConfig.saveAgentPidProperties(String.valueOf(sigar.getPid()), startMode);
	}


	/**
	 * Print help and exit. This is provided for mocking.
	 *
	 * @param message message
	 */
	protected void printHelpAndExit(String message) {
		staticPrintHelpAndExit(message);
	}

	/**
	 * print help and exit. This is provided for mocking.
	 *
	 * @param message message
	 * @param e       exception
	 */
	protected void printHelpAndExit(String message, Exception e) {
		staticPrintHelpAndExit(message, e);
	}


	private static void staticPrintHelpAndExit(String message) {
		staticPrintHelpAndExit(message, null);
	}

	private static void staticPrintHelpAndExit(String message, Exception e) {
		if (e == null) {
			LOG.error(message);
		} else {
			LOG.error(message, e);
		}
		if (modeParam != null) {
			modeParam.usage();
		}
		System.exit(-1);
	}
}
