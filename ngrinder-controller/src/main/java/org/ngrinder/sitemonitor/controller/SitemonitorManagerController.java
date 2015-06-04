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

import java.util.List;

import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.sitemonitor.service.SitemonitorManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Sitemonitor manager controller.
 *
 * @author Gisoo Gwon
 */
@Controller
@RequestMapping("/sitemonitor")
public class SitemonitorManagerController extends BaseController {
	
	@Autowired
	private SitemonitorManagerService sitemonitorManager;
	
	@RequestMapping({"", "/", "/list"})
	public String getAll(ModelMap model) {
		List<AgentInfo> agents = sitemonitorManager.getAllAgentInfo();
		model.addAttribute("agents", agents);
		
		return "sitemonitor/list";
	}
	
}
