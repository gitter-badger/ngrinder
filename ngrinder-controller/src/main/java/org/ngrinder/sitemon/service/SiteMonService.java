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
package org.ngrinder.sitemon.service;

import java.util.Date;
import java.util.List;

import org.ngrinder.common.util.DateUtils;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.SiteMon;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.sitemon.model.SiteMonResult;
import org.ngrinder.sitemon.repository.SiteMonRepository;
import org.ngrinder.sitemon.repository.SiteMonResultRepository;
import org.ngrinder.sitemon.repository.SiteMonResultSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.common.processidentity.AgentIdentity;

/**
 * @author Gisoo Gwon
 */
@Component
public class SiteMonService {

	@Autowired
	private SiteMonAgentManagerService siteMonAgentManagerService;

	@Autowired
	private SiteMonRepository siteMonRepository;
	
	@Autowired
	private SiteMonResultRepository siteMonResultRepository;
	
	@Autowired
	private FileEntryService fileEntryService;

	public List<SiteMon> getAll(User user) {
		List<SiteMon> monitors = siteMonRepository.findByCreatedUser(user);
		initAgentRunning(monitors);
		return monitors;
	}
	
	public List<SiteMonResult> getResultRecentMonth(final String siteMonId) {
		Date ThiryDaysAgo = DateUtils.addDay(new Date(), -30);
		return siteMonResultRepository.findAll(SiteMonResultSpecification.idEqualAndAfterTime(
			siteMonId, ThiryDaysAgo));
	}
	
	public SiteMon getOne(String siteMonId) {
		return siteMonRepository.findOne(siteMonId);
	}
	
	/**
	 * Get SiteMon script from perfTest script.
	 * @param user
	 * @param perfTest The perftest for sitemon.
	 * @param scriptClone clone perftest script.
	 * @return
	 */
	public FileEntry getSiteMonScript(User user, PerfTest perfTest, boolean scriptClone) {
		FileEntry perfTestScript = fileEntryService.getOne(user, perfTest.getScriptName(),
			perfTest.getScriptRevision());
		if (!scriptClone) {
			return perfTestScript;
		}
		FileEntry siteMonScript = new FileEntry();
		siteMonScript.setPath(perfTest.getSiteMonScriptName());
		siteMonScript.setContent(perfTestScript.getContent());
		siteMonScript.setDescription("Clone for sitemon.");
		fileEntryService.save(user, siteMonScript);
		return fileEntryService.getOne(user, siteMonScript.getPath());
	}

	/**
	 * Initialize agent running state.
	 * @param siteMons target SiteMon list
	 */
	private void initAgentRunning(List<SiteMon> siteMons) {
		for (SiteMon siteMon : siteMons) {
			String name = siteMon.getAgentName();
			AgentIdentity identity = siteMonAgentManagerService.getConnectingAgentIdentity(name);
			siteMon.setAgentRunning(identity != null);
		}
	}

}
