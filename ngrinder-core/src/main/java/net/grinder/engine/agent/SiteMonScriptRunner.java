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
package net.grinder.engine.agent;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constants.GrinderConstants;
import org.ngrinder.sitemon.SiteMonControllerServerDaemon;
import org.ngrinder.sitemon.engine.process.SiteMonProcessEntryPoint;
import org.ngrinder.sitemon.messages.ShutdownSiteMonProcessMessage;
import org.ngrinder.sitemon.messages.SiteMonResultMessage;
import org.ngrinder.sitemon.model.SiteMonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.common.GrinderProperties;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.FanOutStreamSender;
import net.grinder.communication.Message;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.Handler;
import net.grinder.communication.StreamSender;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.lang.AbstractLanguageHandler;
import net.grinder.lang.Lang;
import net.grinder.util.AbstractGrinderClassPathProcessor;
import net.grinder.util.Directory;
import net.grinder.util.NetworkUtils;

import static org.ngrinder.common.util.NoOp.noOp;

/**
 * @author Gisoo Gwon
 */
public class SiteMonScriptRunner implements GrinderConstants {

	private Logger LOGGER = LoggerFactory.getLogger("sitemon script runner");
	private Map<String, ProcessWorker> workers = new HashMap<String, ProcessWorker>();
	private final File baseDirectory;
	private ConcurrentLinkedQueue<SiteMonResult> monitoringResults = new ConcurrentLinkedQueue<SiteMonResult>();
	private SiteMonControllerServerDaemon scriptProcessConsole;

	public SiteMonScriptRunner(File baseDirectory) {
		this.baseDirectory = baseDirectory;
		scriptProcessConsole = new SiteMonControllerServerDaemon(
			NetworkUtils.getFreePortOfLocal());
		scriptProcessConsole.start();
		
		ConsoleCommunication console = scriptProcessConsole.getComponent(ConsoleCommunication.class);
		MessageDispatchRegistry messageDispatchRegistry = console.getMessageDispatchRegistry();	
		messageDispatchRegistry.set(SiteMonResultMessage.class, new Handler<SiteMonResultMessage>() {
				@Override
				public void handle(SiteMonResultMessage message) throws CommunicationException {
					synchronized (monitoringResults) {
						monitoringResults.addAll(message.getResults());
					}
				}

				@Override
				public void shutdown() {
					noOp();
				}

			});
	}
	
	/**
	 * Clone and clear to sitemon result list.  
	 * @return Sitemon result list.
	 */
	public List<SiteMonResult> pollAllResult() {
		synchronized (monitoringResults) {
			LinkedList<SiteMonResult> results = new LinkedList<SiteMonResult>(
				monitoringResults);
			monitoringResults.clear();
			return results;
		}
	}

	public void runWorker(String siteMonId, String scriptname, String hosts, String params) {
		FanOutStreamSender fanOutStreamSender = null;
		ProcessWorker worker = null;

		try {
			// create
			File scriptDir = new File(baseDirectory, siteMonId);
			File scriptFile = new File(scriptDir, scriptname);
			fanOutStreamSender = new FanOutStreamSender(1);
			Directory workingDirectory = new Directory(baseDirectory);
			AbstractLanguageHandler handler = Lang.getByFileName(scriptFile).getHandler();
			Properties systemProperties = new Properties();
			GrinderProperties properties = new GrinderProperties();
			PropertyBuilder builder = new PropertyBuilder(properties, new Directory(scriptDir), false,
				hosts, NetworkUtils.getLocalHostName());
			AgentIdentityImplementation agentIdentity = new AgentIdentityImplementation(siteMonId);
			
			// init
			properties.setProperty("sitemon.id", siteMonId);
			AbstractGrinderClassPathProcessor classPathProcessor = handler.getClassPathProcessor();
			String grinderJVMClassPath = classPathProcessor.buildForemostClasspathBasedOnCurrentClassLoader(LOGGER)
				+ File.pathSeparator
				+ classPathProcessor.buildPatchClasspathBasedOnCurrentClassLoader(LOGGER)
				+ File.pathSeparator + builder.buildCustomClassPath(true);
			if (!StringUtils.isBlank(params)) {
				params = params.replace("'", "\\'").replace(" ", "");
				properties.setProperty(GRINDER_PROP_PARAM, params);
			}
			properties.setProperty(GRINDER_PROP_JVM_CLASSPATH, grinderJVMClassPath);
			properties.setProperty(GrinderProperties.CONSOLE_HOST, "127.0.0.1");
			properties.setInt(GrinderProperties.CONSOLE_PORT, scriptProcessConsole.getPort());
			systemProperties.put("java.class.path", baseDirectory.getAbsolutePath() + File.pathSeparator
				+ classPathProcessor.buildClasspathBasedOnCurrentClassLoader(LOGGER));
			agentIdentity.setNumber(0);

			String buildJVMArgumentWithoutMemory = builder.buildJVMArgumentWithoutMemory();
			properties.setProperty("grinder.jvm.classpath", grinderJVMClassPath);

			// logging
			LOGGER.debug("grinder.jvm.classpath  : {} ", grinderJVMClassPath);
			LOGGER.debug("sitemon class path " + classPathProcessor.buildClasspathBasedOnCurrentClassLoader(LOGGER));
			LOGGER.debug("jvm args : {} ", buildJVMArgumentWithoutMemory);

			// worker init
			
			WorkerProcessCommandLine workerCommandLine = new WorkerProcessCommandLine(properties,
				systemProperties, buildJVMArgumentWithoutMemory, workingDirectory,
				SiteMonProcessEntryPoint.class);
			ProcessWorkerFactory workerFactory = new ProcessWorkerFactory(workerCommandLine,
				agentIdentity, fanOutStreamSender, true, new ScriptLocation(scriptFile), properties);
			worker = (ProcessWorker) workerFactory.create(System.out, System.err);

			saveWorker(siteMonId, worker);
			worker.waitFor();
		} catch (Exception e) {
			LOGGER.error("Error while executing {} because {}", siteMonId, e.getMessage());
			LOGGER.info("The error detail is ", e);
			// TODO : send error message to console
		} finally {
			if (fanOutStreamSender != null) {
				fanOutStreamSender.shutdown();
			}
			if (worker != null) {
				worker.destroy();
			}
		}
	}

	public void shutdown() {
		for (Entry<String, ProcessWorker> entry : workers.entrySet()) {
			ProcessWorker processWorker = workers.get(entry.getKey());
			if (processWorker != null) {
				try {
					sendMessage(entry.getKey(), new ShutdownSiteMonProcessMessage());
				} catch (CommunicationException e) {
					LOGGER.warn("{} shutdown failed", entry.getKey());
				}
				processWorker.destroy();
			}
		}
		scriptProcessConsole.shutdown();
	}

	public void saveWorker(String siteMonId, ProcessWorker newWorker) {
		Worker pastWorker = workers.get(siteMonId);
		if (pastWorker != null) {
			pastWorker.destroy();
			LOGGER.debug("destroy " + siteMonId);
		}

		workers.put(siteMonId, newWorker);
	}

	/**
	 * Send message to worker.
	 * 
	 * @param siteMonId
	 * @param message
	 * @return
	 * @throws CommunicationException
	 */
	public boolean sendMessage(String siteMonId, Message message) throws CommunicationException {
		Worker worker = workers.get(siteMonId);
		if (worker != null) {
			new StreamSender(worker.getCommunicationStream()).send(message);
			return true;
		}
		return false;
	}

	public Set<String> getRunningGroups() {
		return workers.keySet();
	}

}
