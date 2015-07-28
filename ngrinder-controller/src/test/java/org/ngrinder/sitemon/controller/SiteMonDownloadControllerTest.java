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
package org.ngrinder.sitemon.controller;

import static org.mockito.Mockito.*;

import java.net.URLClassLoader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.infra.config.Config;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Gisoo Gwon
 * @since 3.4
 */
@RunWith(MockitoJUnitRunner.class)
public class SiteMonDownloadControllerTest {
	@Mock
	AgentPackageService agentPackageService;
	
	@InjectMocks
	SiteMonDownloadController sut = new SiteMonDownloadController();
	
	@Test
	public void testDownload() throws Exception {
		String controllerIp = "111.111.111.111";
		int controllerPort = 12345;
		String owner = "me";
		MockHttpServletResponse response = new MockHttpServletResponse();
		Config config = mock(Config.class);

		sut.setConfig(config);
		
		when(config.getSiteMonAgentControllerIp()).thenReturn(controllerIp);
		when(config.getSiteMonAgentControllerPort()).thenReturn(controllerPort);
		
		sut.download(owner, response);
		
		verify(agentPackageService, times(1)).createSiteMonAgentPackage(
			(URLClassLoader) getClass().getClassLoader(), controllerIp, controllerPort, owner);
	}
}
