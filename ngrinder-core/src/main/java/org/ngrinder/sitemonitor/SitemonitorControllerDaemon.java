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

import org.ngrinder.common.util.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.common.GrinderException;
import net.grinder.engine.agent.Agent;

/**
 * Run {@link SitemonitorController}.
 * If the connection fails, retry before shutdown
 * @author Gisoo Gwon
 * @since 3.4
 */
public class SitemonitorControllerDaemon implements Agent {
	private static final int LOG_FREQUENCY = 5;
	protected static final int CONTROLLER_RETRY_INTERVAL = 2 * 1000;
	private SitemonitorController sitemonitorController;
	private Thread controllerThread;
	private boolean forceShutdown = false;;
	
	public static final Logger LOGGER = LoggerFactory.getLogger("site monitor controller daemon");

	public SitemonitorControllerDaemon(SitemonitorController sitemonitorController) {
		this.sitemonitorController = sitemonitorController;
	}

	@Override
	public void run() throws GrinderException {
		controllerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int count = 0;
				
				do {
					try {
						if (count++ % LOG_FREQUENCY == 0) {
							LOGGER.info("The site monitor daemon is started.");
						}
						sitemonitorController.run();
					} catch (Exception e) {
						LOGGER.info("Sitemonitor controller daemon is crashed. {}", e.getMessage());
						LOGGER.debug("The error detail is  ", e);
					}
					ThreadUtils.sleep(CONTROLLER_RETRY_INTERVAL);
				} while (!forceShutdown);
			}
		}, "Sitemonitor controller thread");
		controllerThread.start();
	}
	
	

	public SitemonitorController getSitemonitorControll() {
		return sitemonitorController;
	}

	/**
	 * Shutdown site monitor controller.
	 */
	@Override
	public void shutdown() {
		try {
			forceShutdown  = true;
			sitemonitorController.shutdown();
			if (controllerThread != null) {
				ThreadUtils.stopQuietly(controllerThread, "Sitemonitor controller thread was not stopped. Stop by force.");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
