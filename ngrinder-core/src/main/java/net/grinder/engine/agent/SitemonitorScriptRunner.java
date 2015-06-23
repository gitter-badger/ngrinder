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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.ngrinder.common.constants.GrinderConstants;
import org.ngrinder.sitemonitor.engine.process.SitemonitorProcessEntryPoint;
import org.ngrinder.sitemonitor.messages.ShutdownSitemonitorProcessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.common.GrinderProperties;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.FanOutStreamSender;
import net.grinder.communication.Message;
import net.grinder.communication.StreamSender;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.lang.AbstractLanguageHandler;
import net.grinder.lang.Lang;
import net.grinder.util.AbstractGrinderClassPathProcessor;
import net.grinder.util.Directory;
import net.grinder.util.NetworkUtils;

/**
 * @author Gisoo Gwon
 */
public class SitemonitorScriptRunner implements GrinderConstants {

	private Logger LOGGER = LoggerFactory.getLogger("site monitor script runner");
	private Map<String, ProcessWorker> workers = new HashMap<String, ProcessWorker>();
	private final int serverConsolePort;
	private final File baseDirectory;

	public SitemonitorScriptRunner(int serverConsolePort, File baseDirectory) {
		this.serverConsolePort = serverConsolePort;
		this.baseDirectory = baseDirectory;
	}

	public void runWorker(String sitemonitorId, String scriptname, String hosts, String params,
		File base) {
		FanOutStreamSender fanOutStreamSender = null;
		ProcessWorker worker = null;

		try {
			// create
			File scriptDir = new File(baseDirectory, sitemonitorId);
			File scriptFile = new File(scriptDir, scriptname);
			fanOutStreamSender = new FanOutStreamSender(1);
			Directory workingDirectory = new Directory(base);
			AbstractLanguageHandler handler = Lang.getByFileName(scriptFile).getHandler();
			Properties systemProperties = new Properties();
			GrinderProperties properties = new GrinderProperties();
			PropertyBuilder builder = new PropertyBuilder(properties, new Directory(scriptDir), false,
				"", NetworkUtils.getLocalHostName());
			AgentIdentityImplementation agentIdentity = new AgentIdentityImplementation(sitemonitorId);
			
			// init
			AbstractGrinderClassPathProcessor classPathProcessor = handler.getClassPathProcessor();
			String grinderJVMClassPath = classPathProcessor.buildForemostClasspathBasedOnCurrentClassLoader(LOGGER)
				+ File.pathSeparator
				+ classPathProcessor.buildPatchClasspathBasedOnCurrentClassLoader(LOGGER)
				+ File.pathSeparator + builder.buildCustomClassPath(true);
			properties.setProperty("grinder.jvm.classpath", grinderJVMClassPath);
			properties.setProperty("grinder.consoleHost", "localhost");
			properties.setInt("grinder.consolePort", serverConsolePort);
			systemProperties.put("java.class.path", base.getAbsolutePath() + File.pathSeparator
				+ classPathProcessor.buildClasspathBasedOnCurrentClassLoader(LOGGER));
			agentIdentity.setNumber(0);

			String buildJVMArgumentWithoutMemory = builder.buildJVMArgumentWithoutMemory();
			properties.setProperty("grinder.jvm.classpath", grinderJVMClassPath);

			// logging
			LOGGER.info("grinder.jvm.classpath  : {} ", grinderJVMClassPath);
			LOGGER.debug("sitemonitor class path " + classPathProcessor.buildClasspathBasedOnCurrentClassLoader(LOGGER));
			LOGGER.info("jvm args : {} ", buildJVMArgumentWithoutMemory);

			// worker init
			
			WorkerProcessCommandLine workerCommandLine = new WorkerProcessCommandLine(properties,
				systemProperties, buildJVMArgumentWithoutMemory, workingDirectory,
				SitemonitorProcessEntryPoint.class);
			ProcessWorkerFactory workerFactory = new ProcessWorkerFactory(workerCommandLine,
				agentIdentity, fanOutStreamSender, true, new ScriptLocation(scriptFile), properties);
			worker = (ProcessWorker) workerFactory.create(System.out, System.err);

			saveWorker(sitemonitorId, worker);
			worker.waitFor();
		} catch (Exception e) {
			LOGGER.error("Error while executing {} because {}", sitemonitorId, e.getMessage());
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
					sendMessage(entry.getKey(), new ShutdownSitemonitorProcessMessage());
				} catch (CommunicationException e) {
					LOGGER.warn("{} shutdown failed", entry.getKey());
				}
				processWorker.destroy();
			}
		}
	}

	public void saveWorker(String sitemonitorId, ProcessWorker newWorker) {
		Worker pastWorker = workers.get(sitemonitorId);
		if (pastWorker != null) {
			pastWorker.destroy();
			LOGGER.debug("destroy " + sitemonitorId);
		}

		workers.put(sitemonitorId, newWorker);
	}

	/**
	 * Send message to worker.
	 * 
	 * @param sitemonitorId
	 * @param message
	 * @return
	 * @throws CommunicationException
	 */
	public boolean sendMessage(String sitemonitorId, Message message) throws CommunicationException {
		Worker worker = workers.get(sitemonitorId);
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
