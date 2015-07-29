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

import static org.ngrinder.common.util.ExceptionUtils.*;

import java.io.File;
import java.net.URLClassLoader;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletResponse;

import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.FileDownloadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * SiteMonAgent Download Controller.
 *
 * @author Gisoo-Gwon
 */
@Controller
@RequestMapping("/sitemon")
public class SiteMonDownloadController extends BaseController {
	
	@Autowired
	private AgentPackageService agentPackageService;
	
	/**
	 * Download site mon agent.
	 *
	 * @param response response.
	 * @throws UnknownHostException 
	 */
	@RequestMapping(value = "/download")
	public void download(	@RequestParam(value = "owner", required = false) String owner,
        					HttpServletResponse response) throws UnknownHostException {
		String ip = getConfig().getSiteMonAgentControllerIp();
		int port = getConfig().getSiteMonAgentControllerPort();
		
		try {
			final File siteMonAgentPackage = agentPackageService.createSiteMonAgentPackage(
				(URLClassLoader) getClass().getClassLoader(), ip, port, owner);
			FileDownloadUtils.downloadFile(response, siteMonAgentPackage);
		} catch (Exception e) {
			throw processException(e);
		}
	}
}
