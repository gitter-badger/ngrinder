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

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.sitemonitor.SitemonitorController;
import org.ngrinder.sitemonitor.SitemonitorControllerDaemon;

public class SitemonitorControllerDaemonTest {
	private static final int LITTLE_TIME = 100;
	
	SitemonitorController controller;
	SitemonitorControllerDaemon daemon;
	
	@Before
	public void before() {
		controller = mock(SitemonitorController.class);
		daemon = new SitemonitorControllerDaemon(controller);
	}
	
	@After
	public void after() {
		daemon.shutdown();
	}
	
	@Test
	public void testRetry() throws Exception {		
		doThrow(new RuntimeException("exception for JUnit test")).when(controller).run();
		
		daemon.run();
		ThreadUtils.sleep(LITTLE_TIME);
		
		verify(controller, times(1)).run();
		for (int retry = 2; retry < 5; retry++) {
			ThreadUtils.sleep(SitemonitorControllerDaemon.CONTROLLER_RETRY_INTERVAL);
			verify(controller, times(retry)).run();
		}
	}
	
	@Test
	public void testShutdown() throws Exception {
		doThrow(new RuntimeException("exception for JUnit test")).when(controller).run();
		
		daemon.run();
		ThreadUtils.sleep(LITTLE_TIME);

		verify(controller, times(1)).run();
		daemon.shutdown();
		
		ThreadUtils.sleep(SitemonitorControllerDaemon.CONTROLLER_RETRY_INTERVAL);
		verify(controller, times(1)).shutdown();		
		verify(controller, atMost(1)).run();
	}
}
