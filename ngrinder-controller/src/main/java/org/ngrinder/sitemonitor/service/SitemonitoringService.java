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
package org.ngrinder.sitemonitor.service;

import java.util.List;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Sitemonitoring;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.sitemonitor.repository.SitemonitoringRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.common.processidentity.AgentIdentity;

/**
 * @author Gisoo Gwon
 */
@Component
public class SitemonitoringService {

	@Autowired
	private SitemonitorManagerService sitemonitorManagerService;

	@Autowired
	private SitemonitoringRepository sitemonitoringRepository;
	
	@Autowired
	private FileEntryService fileEntryService;

	public List<Sitemonitoring> getSitemonitoringsOf(User user) {
		List<Sitemonitoring> monitors = sitemonitoringRepository.findByRegistUser(user);
		initAgentRunning(monitors);
		return monitors;
	}
	
	public Sitemonitoring getSitemonitoring(String sitemonitoringId) {
		return sitemonitoringRepository.findOne(sitemonitoringId);
	}
	
	/**
	 * Get Sitemonitoring script from perfTest script.
	 * @param user
	 * @param perfTest The perftest for sitemonitoring.
	 * @param isScriptClone clone perftest script.
	 * @return
	 */
	public FileEntry getSitemonitoringScript(User user, PerfTest perfTest, boolean isScriptClone) {
		FileEntry perfTestScript = fileEntryService.getOne(user, perfTest.getScriptName(),
			perfTest.getScriptRevision());
		if (isScriptClone) {
			FileEntry sitemonitoringScript = new FileEntry();
			sitemonitoringScript.setPath(perfTest.getSitemonitoringScriptName());
			sitemonitoringScript.setContent(perfTestScript.getContent());
			sitemonitoringScript.setDescription("Clone for sitemonitoring.");
			fileEntryService.save(user, sitemonitoringScript);
			return fileEntryService.getOne(user, sitemonitoringScript.getPath());
		} else {
			return perfTestScript;
		}
	}

	/**
	 * Set agent running of Sitemonitor.agentRunning(bool)
	 * @param sitemonitorings target Sitemonitoring list
	 */
	private void initAgentRunning(List<Sitemonitoring> sitemonitorings) {
		for (Sitemonitoring sitemonitor : sitemonitorings) {
			String name = sitemonitor.getAgentName();
			AgentIdentity identity = sitemonitorManagerService.getConnectingAgentIdentity(name);
			sitemonitor.setAgentRunning(identity != null);
		}
	}

}
