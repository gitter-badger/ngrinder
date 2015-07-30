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

import static org.ngrinder.common.util.Preconditions.*;

import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.SiteMon;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.sitemon.service.SiteMonAgentManagerService;
import org.ngrinder.sitemon.service.SiteMonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.grinder.util.Directory.DirectoryException;
import net.grinder.util.FileContents.FileContentsException;

/**
 * SiteMon regist/unregist Controller.
 * 
 * @author Gisoo Gwon
 */
@Controller
@RequestMapping("/sitemon")
public class SiteMonController extends BaseController {
	
	@Autowired
	private SiteMonService siteMonService;

	@Autowired
	private SiteMonAgentManagerService siteMonAgentManagerService;
	
	@Autowired
	private PerfTestService perfTestService;
	
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping({"/agent", "/agent/list"})
	public String getAllAgent(ModelMap model) {
		model.addAttribute("allAgentStatus", siteMonAgentManagerService.getAllAgentStatus());
		return "sitemon/agent/list";
	}
	
	@RequestMapping({"", "/", "/list"})
	public String list(User user, ModelMap modelMap) {
		modelMap.put("siteMons", siteMonService.getAll(user));
		return "sitemon/list";
	}
	
	@RequestMapping(value = "/new_from_perftest/{perfTestId}", method = RequestMethod.GET)
	public String openForm(User user, @PathVariable Long perfTestId,
			@RequestParam boolean scriptClone, ModelMap modelMap) {
		PerfTest perfTest = perfTestService.getOne(perfTestId);
		checkNotNull(perfTest, "no perftest for %s exits", perfTestId);
		checkTrue(hasPermission(perfTest.getCreatedUser()), "invalid permission for " + perfTestId + " perftest");

		FileEntry siteMonScript = siteMonService.getSiteMonScript(user, perfTest, scriptClone);
		modelMap.addAttribute("siteMon", new SiteMon("Perftest" + perfTestId, user,
			siteMonScript.getPath(), siteMonScript.getRevision(), perfTest.getTargetHosts(),
			perfTest.getParam(), null));
		return "sitemon/detail";
	}
	
	@RequestMapping("/save")
	public String saveOne(User user, SiteMon siteMon, ModelMap modelMap)
			throws FileContentsException, DirectoryException {
		siteMon.setCreatedUser(user);
		siteMon.setAgentName(siteMonAgentManagerService.getIdleResouceAgentName());
		modelMap.addAttribute("msg", siteMonAgentManagerService.sendRegist(siteMon));
		return "sitemon/detail";
	}
	
	@RestAPI
	@RequestMapping("/api/del/{siteMonId}")
	public HttpEntity<String> delete(User user, @PathVariable String siteMonId) {
		SiteMon siteMon = siteMonService.getOne(siteMonId);
		checkNotNull(siteMon, "No exists {} sitemon.", siteMonId);
		checkTrue(hasPermission(siteMon.getCreatedUser()), "invalid permission for " + siteMonId);
		siteMonAgentManagerService.delSiteMon(user , siteMonId);
		return toJsonHttpEntity("success");
	}
	
	@RequestMapping("/get/{siteMonId}")
	public String getOne(User user, @PathVariable String siteMonId, ModelMap modelMap) {
		SiteMon siteMon = siteMonService.getOne(siteMonId);
		checkNotNull(siteMon, "no siteMon for %s exists", siteMonId);
		checkTrue(hasPermission(siteMon.getCreatedUser()), "invalid permission for " + siteMonId + " sitemon");
		modelMap.addAttribute("siteMon", siteMon);
		return "sitemon/detail";
	}
	
	@RestAPI
	@RequestMapping("/api/{siteMonId}/result")
	public HttpEntity<String> getResult(@PathVariable String siteMonId, ModelMap modelMap) {
		return toJsonHttpEntity(siteMonService.getGraphDataRecentDay(siteMonId));
	}
	
	private boolean hasPermission(User createdUser) {
		User user = currentUser();
		if (user == null || user.getRole() == null) {
			return false;
		}
		return user.getRole().equals(Role.ADMIN)
			|| user.getRole().equals(Role.SUPER_USER)
			|| user.equals(createdUser);
	}
	
}
