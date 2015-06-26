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
package org.ngrinder.sitemonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.sitemonitor.messages.RegistScheduleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.engine.agent.SitemonitorScriptRunner;
import net.grinder.util.NetworkUtils;

/**
 * Managing process for execute sitemonitoring script.
 * 
 * @author Gisoo Gwon
 */
public class MonitorSchedulerImplementation implements MonitorScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger("monitor scheduler impl");
	private static final int THREAD_POOL_SIZE = 10;
	private static final long ONE_MINIUTE = 60 * 1000;

	private final SitemonitorScriptRunner scriptRunner;
	private final File baseDirectory;

	private SitemonitorControllerServerDaemon serverDaemon;
	private ExecutorService executor;

	Map<String, RegistScheduleMessage> sitemonitorMap = new HashMap<String, RegistScheduleMessage>();

	/**
	 * The constructor.
	 * 
	 * @param agentConfig
	 */
	public MonitorSchedulerImplementation(File baseDirectory) {
		this.baseDirectory = baseDirectory;
		serverDaemon = new SitemonitorControllerServerDaemon(NetworkUtils.getFreePortOfLocal());
		serverDaemon.start();
		executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		scriptRunner = new SitemonitorScriptRunner(serverDaemon.getPort(), baseDirectory);
		new ScriptRunner().start();
	}

	/**
	 * @param sitemonitorId
	 * @param scriptname
	 */
	@Override
	public void regist(final RegistScheduleMessage message) {
		final String sitemonitorId = message.getSitemonitorId();
		sitemonitorMap.put(sitemonitorId, message);
	}

	/**
	 * @param groupName
	 */
	@Override
	public void unregist(String sitemonitorId) {
		sitemonitorMap.remove(sitemonitorId);
	}

	/**
	 * destroy process.
	 */
	@Override
	public void shutdown() {
		scriptRunner.shutdown();
		serverDaemon.shutdown();
	}

	class ScriptRunner extends Thread {

		ScriptRunner() {
			setDaemon(true);
		}

		@Override
		public void run() {
			List<Future<Object>> futures = new ArrayList<Future<Object>>();
			while (true) {
				LOGGER.debug("Sitemonitor runner awake! regist sitemonitor cnt is {}",
					sitemonitorMap.size());

				long st = System.currentTimeMillis();
				futures.clear();
				runScriptUsingThreadPool(futures);
				waitScriptComplete(futures);
				long end = System.currentTimeMillis();
				sleepForRepeatCycle(end - st);
			}
		}

		private void sleepForRepeatCycle(long usedTime) {
			if (usedTime < ONE_MINIUTE) {
				ThreadUtils.sleep(ONE_MINIUTE - (usedTime));
			}
		}

		private void waitScriptComplete(List<Future<Object>> futures) {
			for (Future<Object> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
					LOGGER.error("script run failed {}", e.getMessage());
				}
			}
		}

		private void runScriptUsingThreadPool(List<Future<Object>> futures) {
			for (Entry<String, RegistScheduleMessage> entry : sitemonitorMap.entrySet()) {
				final String sitemonitorId = entry.getKey();
				final RegistScheduleMessage message = entry.getValue();

				Callable<Object> task = new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						scriptRunner.runWorker(sitemonitorId, message.getScriptname(),
							message.getPropHosts(), message.getPropParam(), baseDirectory);
						return null;
					}
				};
				futures.add(executor.submit(task));
				LOGGER.debug("submit task for {}", sitemonitorId);
			}
		}
	}

}
