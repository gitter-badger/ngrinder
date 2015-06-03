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
package org.ngrinder.sitemonitor.controller;

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
 * Sitemonitor Download Controller.
 *
 * @author Gisoo-Gwon
 * @since 3.4
 */
@Controller
@RequestMapping("/sitemonitor")
public class SitemonitorDownloadController extends BaseController {
	
	@Autowired
	private AgentPackageService agentPackageService;
	
	/**
	 * Download sitemonitor.
	 *
	 * @param response response.
	 * @throws UnknownHostException 
	 */
	@RequestMapping(value = "/download")
	public void download(	@RequestParam(value = "owner", required = false) String owner,
        					HttpServletResponse response) throws UnknownHostException {
		String ip = getConfig().getSitemonitorControllerIp();
		int port = getConfig().getSitemonitorControllerPort();
		
		try {
			final File sitemonitorPackage = agentPackageService.createSitemonitorPackage(
				(URLClassLoader) getClass().getClassLoader(), ip, port, owner);
			FileDownloadUtils.downloadFile(response, sitemonitorPackage);
		} catch (Exception e) {
			throw processException(e);
		}
	}
}
