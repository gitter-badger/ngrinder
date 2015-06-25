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
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.sitemonitor.service.SitemonitorManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import net.grinder.util.Directory.DirectoryException;
import net.grinder.util.FileContents.FileContentsException;

/**
 * @author Gisoo Gwon
 */
@Controller
@RequestMapping("/sitemonitor/api")
public class SitemonitorApiController extends BaseController {

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private SitemonitorManagerService sitemonitorManagerService;

	/**
	 * Add sitemonitoring to agent by perftest.
	 * 
	 * @param user			user
	 * @param perfTestId	perftest id
	 * @throws DirectoryException 
	 * @throws FileContentsException 
	 */
	@RestAPI
	@RequestMapping("/add/{perfTestId}")
	public HttpEntity<String> addSitemonitoring(User user,
			@PathVariable("perfTestId") Long perfTestId) throws FileContentsException,
			DirectoryException {
		PerfTest perfTest = perfTestService.getOne(perfTestId);
		checkNotNull(perfTest, "no perftest for %s exits", perfTestId);
		checkTrue(hasGrant(perfTest, user), "invalid grant for " + perfTestId + " perftest");

		String sitemonitorId = "Perftest" + perfTestId;
		FileEntry script = fileEntryService.getOne(user, perfTest.getScriptName(),
			perfTest.getScriptRevision());
		checkNotNull(perfTest, "Script file '%s' does not exist", perfTest.getScriptName());

		sitemonitorManagerService.addSitemonitoring(user, sitemonitorId, script,
			perfTest.getTargetHosts(), perfTest.getParam());

		return toJsonHttpEntity("success");
	}
	
	@RestAPI
	@RequestMapping("/del/{sitemonitoringId}")
	public HttpEntity<String> delSitemonitoring(User user,
			@PathVariable("sitemonitoringId") String sitemonitoringId) {
		sitemonitorManagerService.delSitemonitoring(user , sitemonitoringId);
		return toJsonHttpEntity("success");
	}

	private boolean hasGrant(PerfTest perfTest, User user) {
		return user.equals(perfTest.getCreatedUser());
	}

}
