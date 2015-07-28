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

import static org.ngrinder.common.util.Preconditions.*;

import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.Sitemonitoring;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.sitemonitor.model.SitemonitoringResult;
import org.ngrinder.sitemonitor.service.SitemonitorManagerService;
import org.ngrinder.sitemonitor.service.SitemonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.grinder.util.Directory.DirectoryException;
import net.grinder.util.FileContents.FileContentsException;

/**
 * Sitemonitoring regist/unregist Controller.
 * 
 * @author Gisoo Gwon
 */
@Controller
@RequestMapping("/sitemonitoring")
public class SitemonitoringController extends BaseController {
	
	@Autowired
	private SitemonitoringService sitemonitoringService;

	@Autowired
	private SitemonitorManagerService sitemonitorManagerService;
	
	@Autowired
	private PerfTestService perfTestService;
	
	@RequestMapping({"", "/", "/list"})
	public String list(User user, ModelMap modelMap) {
		modelMap.put("sitemonitorings", sitemonitoringService.getSitemonitoringsOf(user));
		return "sitemonitoring/list";
	}
	
	@RequestMapping(value = "/new/{perfTestId}/{isScriptClone}", method = RequestMethod.GET)
	public String openForm(User user, @PathVariable("perfTestId") Long perfTestId,
			@PathVariable("isScriptClone") boolean isScriptClone, ModelMap modelMap) {
		PerfTest perfTest = perfTestService.getOne(perfTestId);
		checkNotNull(perfTest, "no perftest for %s exits", perfTestId);
		checkTrue(hasGrant(perfTest, user), "invalid grant for " + perfTestId + " perftest");

		FileEntry sitemonitoringScript =  sitemonitoringService.getSitemonitoringScript(user, perfTest, isScriptClone);
		modelMap.addAttribute("sitemonitoring", new Sitemonitoring("Perftest" + perfTestId, user,
			sitemonitoringScript.getPath(), sitemonitoringScript.getRevision(), perfTest.getTargetHosts(),
			perfTest.getParam(), null));
		return "sitemonitoring/new";
	}
	
	@RequestMapping("/save")
	public String saveSitemonitoring(User user, Sitemonitoring sitemonitoring, ModelMap modelMap)
			throws FileContentsException, DirectoryException {
		sitemonitoring.setRegistUser(user);
		sitemonitoring.setAgentName(sitemonitorManagerService.getIdleResouceAgentName());
		modelMap.addAttribute("msg", sitemonitorManagerService.addSitemonitoring(sitemonitoring));
		return "sitemonitoring/new";
	}
	
	@RequestMapping("/get/{sitemonitoringId}")
	public String getSitemonitoring(User user,
		@PathVariable("sitemonitoringId") String sitemonitoringId, ModelMap modelMap) {
		Sitemonitoring sitemonitoring = sitemonitoringService.getSitemonitoring(sitemonitoringId);
		checkNotNull(sitemonitoring, "no sitemonitoring for %s exists", sitemonitoringId);
		checkTrue(hasGrant(sitemonitoring, user), "invalid grant for " + sitemonitoringId + " sitemonitoring");
		modelMap.addAttribute("sitemonitoring", sitemonitoring);
		modelMap.addAttribute("sitemonitoringResult",
			sitemonitoringService.getResultRecentMonth(sitemonitoringId));
		return "sitemonitoring/new";
	}

	private boolean hasGrant(PerfTest perfTest, User user) {
		return user.getRole().equals(Role.ADMIN)
			|| user.getRole().equals(Role.SUPER_USER)
			|| user.equals(perfTest.getCreatedUser());
	}

	private boolean hasGrant(Sitemonitoring sitemonitoring, User user) {
		return user.getRole().equals(Role.ADMIN)
			|| user.getRole().equals(Role.SUPER_USER)
			|| user.equals(sitemonitoring.getRegistUser());
	}
	
}
