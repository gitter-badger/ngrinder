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

import org.ngrinder.model.ScriptType;
import org.ngrinder.sitemonitor.SitemonitorSetting;
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
public class SitemonitorScriptRunner {

	private Logger LOGGER = LoggerFactory.getLogger("site monitor script runner");
	private Map<String, ProcessWorker> workers = new HashMap<String, ProcessWorker>();
	private final int serverConsolePort;

	public SitemonitorScriptRunner(int serverConsolePort) {
		this.serverConsolePort = serverConsolePort;
	}

	public Thread initWithThread(final SitemonitorSetting sitemonitorSetting, final File base) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				init(sitemonitorSetting, base);
			}
		});

		thread.start();
		return thread;
	}

	private void init(SitemonitorSetting sitemonitorSetting, File base) {
		FanOutStreamSender fanOutStreamSender = null;
		ProcessWorker worker = null;
		String groupName = sitemonitorSetting.getGroupName();
		ScriptType scriptType = sitemonitorSetting.getScriptType();

		try {
			// create
			fanOutStreamSender = new FanOutStreamSender(1);
			Directory workingDirectory = new Directory(base);
			AbstractLanguageHandler handler = Lang.getByFileName(scriptType.getTmpScript()).getHandler();
			AbstractGrinderClassPathProcessor classPathProcessor = handler.getClassPathProcessor();
			Properties systemProperties = new Properties();
			GrinderProperties properties = new GrinderProperties();
			AgentIdentityImplementation agentIdentity = new AgentIdentityImplementation(groupName);

			// init
			agentIdentity.setNumber(0);
			properties.setProperty("grinder.errorCallback", sitemonitorSetting.getErrorCallback());
			properties.setInt("grinder.repeatCycle", sitemonitorSetting.getRepeatCycle());
			properties.setProperty("grinder.consoleHost", "localhost");
			properties.setInt("grinder.consolePort", serverConsolePort);
			String newClassPath = classPathProcessor.buildClasspathBasedOnCurrentClassLoader(LOGGER);
			PropertyBuilder builder = new PropertyBuilder(properties, new Directory(base), false,
				"", NetworkUtils.getLocalHostName());
			String buildJVMArgumentWithoutMemory = builder.buildJVMArgumentWithoutMemory();
			String grinderJVMClassPath = classPathProcessor.buildForemostClasspathBasedOnCurrentClassLoader(LOGGER)
				+ File.pathSeparator
				+ classPathProcessor.buildPatchClasspathBasedOnCurrentClassLoader(LOGGER)
				+ File.pathSeparator + builder.buildCustomClassPath(true);
			systemProperties.put("java.class.path", base.getAbsolutePath() + File.pathSeparator
				+ newClassPath);
			properties.setProperty("grinder.jvm.classpath", grinderJVMClassPath);

			// logging
			LOGGER.info("grinder.jvm.classpath  : {} ", grinderJVMClassPath);
			LOGGER.debug("sitemonitor class path " + newClassPath);
			LOGGER.info("jvm args : {} ", buildJVMArgumentWithoutMemory);

			// worker init
			WorkerProcessCommandLine workerCommandLine = new WorkerProcessCommandLine(properties,
				systemProperties, buildJVMArgumentWithoutMemory, workingDirectory,
				SitemonitorProcessEntryPoint.class);
			ProcessWorkerFactory workerFactory = new ProcessWorkerFactory(workerCommandLine,
				agentIdentity, fanOutStreamSender, true, new ScriptLocation(scriptType.getTmpScript()),
				properties);
			worker = (ProcessWorker) workerFactory.create(System.out, System.err);

			saveWorker(groupName, worker);
			worker.waitFor();
		} catch (Exception e) {
			LOGGER.error("Error while executing {} because {}", groupName, e.getMessage());
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
					LOGGER.error("{} shutdown failed", entry.getKey());
				}
				processWorker.destroy();
			}
		}
	}

	public void saveWorker(String groupName, ProcessWorker newWorker) {
		Worker pastWorker = workers.get(groupName);
		if (pastWorker != null) {
			pastWorker.destroy();
			System.out.println("destroy " + groupName);
		}

		workers.put(groupName, newWorker);
	}

	public boolean sendMessage(String groupName, Message message) throws CommunicationException {
		Worker groupWorker = workers.get(groupName);
		if (groupWorker != null) {
			new StreamSender(groupWorker.getCommunicationStream()).send(message);
			return true;
		}
		return false;
	}
	
	public Set<String> getRunningGroups() {
		return workers.keySet();
	}
	
}
