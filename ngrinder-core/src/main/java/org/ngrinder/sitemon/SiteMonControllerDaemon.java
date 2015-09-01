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
package org.ngrinder.sitemon;

import org.ngrinder.common.util.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.common.GrinderException;
import net.grinder.engine.agent.Agent;

/**
 * Run {@link SiteMonController}.
 * If the connection fails, retry before shutdown
 * 
 * @author Gisoo Gwon
 */
public class SiteMonControllerDaemon implements Agent {
	private static final int LOG_FREQUENCY = 5;
	protected static final int CONTROLLER_RETRY_INTERVAL = 2 * 1000;
	private SiteMonController siteMonController;
	private Thread controllerThread;
	private boolean forceShutdown = false;;
	
	public static final Logger LOGGER = LoggerFactory.getLogger("sitemon controller daemon");

	public SiteMonControllerDaemon(SiteMonController siteMonController) {
		this.siteMonController = siteMonController;
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
							LOGGER.info("The sitemon daemon is started.");
						}
						siteMonController.run();
					} catch (Exception e) {
						LOGGER.info("sitemon controller daemon is crashed. {}", e.getMessage());
						LOGGER.debug("The error detail is  ", e);
					}
					ThreadUtils.sleep(CONTROLLER_RETRY_INTERVAL);
				} while (!forceShutdown);
			}
		}, "sitemon controller thread");
		controllerThread.start();
	}

	public SiteMonController getSiteMonController() {
		return siteMonController;
	}

	/**
	 * Shutdown sitemon controller.
	 */
	@Override
	public void shutdown() {
		try {
			forceShutdown  = true;
			siteMonController.shutdown();
			if (controllerThread != null) {
				ThreadUtils.stopQuietly(controllerThread, "Sitemon controller thread was not stopped. Stop by force.");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
