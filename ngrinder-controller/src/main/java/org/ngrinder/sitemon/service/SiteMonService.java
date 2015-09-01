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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ngrinder.common.util.DateUtils;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.SiteMon;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.sitemon.model.SiteMonResult;
import org.ngrinder.sitemon.model.SiteMonJsonResult;
import org.ngrinder.sitemon.repository.SiteMonRepository;
import org.ngrinder.sitemon.repository.SiteMonResultRepository;
import org.ngrinder.sitemon.repository.SiteMonResultSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import net.grinder.common.processidentity.AgentIdentity;

/**
 * {@link SiteMon} and {@link SiteMonResult} service class.
 * 
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
	
	public List<SiteMonResult> getResultRecentDay(final String siteMonId) {
		Date daysAgo = DateUtils.addDay(new Date(), -1);
		return siteMonResultRepository.findAll(SiteMonResultSpecification.idEqualAndAfterTimeOrderByTime(
			siteMonId, daysAgo));
	}
	
	public Map<String, Object> getGraphDataRecentDay(String siteMonId) {
		Date daysAgo = DateUtils.addDay(new Date(), -1);
		return getGraphData(siteMonId, daysAgo);
	}
	
	public Map<String, Object> getGraphData(String siteMonId, Date start) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Specification<SiteMonResult> spec = SiteMonResultSpecification.idEqualAndAfterTimeOrderByTime(
			siteMonId, start);
		List<SiteMonResult> resultData = siteMonResultRepository.findAll(spec);
		SiteMonJsonResult json = new SiteMonJsonResult(resultData);
		resultMap.put("labels", json.getLabelsList());
		resultMap.put("minTimestamp", json.getMinTimestamp());
		resultMap.put("maxTimestamp", json.getMaxTimestamp());
		resultMap.put("successData", json.getSuccessList());
		resultMap.put("errorData", json.getErrorList());
		resultMap.put("testTimeData", json.getTestTimeList());
		
		return resultMap;
	}
	
	public SiteMon getOne(String siteMonId) {
		return siteMonRepository.findOne(siteMonId);
	}
	
	public SiteMon save(SiteMon siteMon) {
		return siteMonRepository.save(siteMon);
	}

	public void delete(String siteMonId) {
		if (siteMonRepository.findOne(siteMonId) != null) {
			siteMonRepository.delete(siteMonId);
		}
	}

	public void updateRunAndAgentName(String siteMonId, boolean run, String agentName) {
		SiteMon siteMon = siteMonRepository.findOne(siteMonId);
		if (siteMon == null) {
			return;
		}
		siteMon.setRunState(run);
		siteMonRepository.saveAndFlush(siteMon);
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
	 * Get error log.
	 * @param siteMonId
	 * @param testNumber
	 * @param date
	 * @return
	 */
	public List<String> getLog(String siteMonId, Date minTimestamp, Date maxTimestamp) {
		return siteMonResultRepository.findErrorLog(siteMonId, minTimestamp, maxTimestamp);
	}

	/**
	 * Initialize agent running state.
	 * @param siteMons target SiteMon list
	 */
	private void initAgentRunning(List<SiteMon> siteMons) {
		for (SiteMon siteMon : siteMons) {
			String name = siteMon.getAgentName();
			AgentIdentity identity = siteMonAgentManagerService.findAgentIdentity(name);
			siteMon.setAgentRunning(identity != null);
		}
	}

}
