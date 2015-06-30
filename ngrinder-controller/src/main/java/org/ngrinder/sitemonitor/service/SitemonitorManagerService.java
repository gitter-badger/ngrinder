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

import static org.ngrinder.common.util.Preconditions.*;

import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.model.Home;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Sitemonitoring;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ProcessingResultPrintStream;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.sitemonitor.SitemonitorControllerServerDaemon;
import org.ngrinder.sitemonitor.messages.RegistScheduleMessage;
import org.ngrinder.sitemonitor.messages.UnregistScheduleMessage;
import org.ngrinder.sitemonitor.model.SitemonitorDistDirectory;
import org.ngrinder.sitemonitor.repository.SitemonitoringRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.Directory;
import net.grinder.util.Directory.DirectoryException;
import net.grinder.util.FileContents.FileContentsException;

/**
 * @author Gisoo Gwon
 */
@Component
public class SitemonitorManagerService implements ControllerConstants {

	public static final Logger LOGGER = LoggerFactory.getLogger(SitemonitorManagerService.class);
	
	SitemonitorControllerServerDaemon sitemonitorServerDaemon;
	
	@Autowired
	private Config config;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;
	
	@Autowired
	private SitemonitoringRepository sitemonitoringRepository;

	/**
	 * Initialize sitemonitor manager.
	 */
	@PostConstruct
	public void init() {
		int port = config.getSitemonitorControllerPort();
		sitemonitorServerDaemon = new SitemonitorControllerServerDaemon(port);
		sitemonitorServerDaemon.start();
	}

	/**
	 * Shutdown sitemonitor controller server.
	 */
	@PreDestroy
	public void destroy() {
		sitemonitorServerDaemon.shutdown();
	}
	
	public Set<AgentStatus> getAllAgentStatus() {
		return sitemonitorServerDaemon.getAllAgentStatus();
	}
	
	/**
	 * Check running agent by Connecting Agent list
	 * @param agentName
	 * @return
	 */
	public boolean isRunningAgent(String agentName) {
		for (AgentIdentity identity : getAllAgents()) {
			if (identity.getName().equals(agentName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return connecting agent list.
	 */
	public Set<AgentIdentity> getAllAgents() {
		return sitemonitorServerDaemon.getAllAvailableAgents();
	}

	/**
	 * If exist sitemonitoringId, remove old sitemonitoring.
	 * Distribute script file and resource to agent.
	 * Send run command to agent.
	 * 
	 * @param user regist user
	 * @param sitemonitorId	sitemonitoring id
	 * @param script		script entity
	 * @param targetHosts	host setting
	 * @param param			system param
	 * @return 
	 * @throws FileContentsException
	 * @throws DirectoryException
	 */
	public String addSitemonitoring(User user, String sitemonitorId, FileEntry script,
			String targetHosts, String param) throws FileContentsException, DirectoryException {
		Set<AgentIdentity> allAgents = sitemonitorServerDaemon.getAllAvailableAgents();
		if (allAgents.size() == 0) {
			return "no have agent";
		}
		
		unregistSitemonitoring(sitemonitorId);
		SitemonitorDistDirectory tmpDistDir = prepareDistributeFile(user, sitemonitorId, script);
		AgentIdentity targetAgent = registSitemonitoringToAgent(sitemonitorId, script, targetHosts,
			param, tmpDistDir);
		sitemonitoringRepository.save(new Sitemonitoring(sitemonitorId, user,
			script.getFileName(), script.getRevision(), targetHosts, param,
			targetAgent.getName()));
		FileUtils.deleteQuietly(tmpDistDir.getRootDirectory());
		return "success";
	}

	/**
	 * Delete sitemonitoring info in DB.
	 * Send unregist command to target agent. 
	 * 
	 * @param user	request user
	 * @param sitemonitoringId	sitemonitoring id
	 */
	public void delSitemonitoring(User user, String sitemonitoringId) {
		Sitemonitoring sitemonitoring = sitemonitoringRepository.findOne(sitemonitoringId);
		checkNotNull(sitemonitoring);
		checkTrue(sitemonitoring.getRegistUser().getId() == user.getId(), 
			"No have grant user {}" + user.getUserName());
		
		unregistSitemonitoring(sitemonitoringId);
	}

	private void unregistSitemonitoring(String sitemonitoringId) {
		Sitemonitoring sitemonitoring = sitemonitoringRepository.findOne(sitemonitoringId);
		if (sitemonitoring != null) {
			unregistSitemonitoringToAgent(sitemonitoring.getId(),
				sitemonitoring.getAgentName(), sitemonitoring.getScriptName());
			sitemonitoringRepository.delete(sitemonitoringId);
		}
	}

	private void unregistSitemonitoringToAgent(String sitemonitorId, String agentName,
			String scriptName) {
		for (AgentIdentity identity : sitemonitorServerDaemon.getAllAvailableAgents()) {
			if (identity.getName().equals(agentName)) {
				AgentAddress agentAddress = new AgentAddress(identity);
				sitemonitorServerDaemon.sendToAddressedAgents(agentAddress,
					new UnregistScheduleMessage(sitemonitorId, scriptName));
			}
		}
	}

	private AgentIdentity registSitemonitoringToAgent(String sitemonitorId, FileEntry script,
			String hosts, String param, SitemonitorDistDirectory tmpDistDirectory)
				throws FileContentsException, DirectoryException {
		AgentIdentity targetAgent = getBestTargetAgent();
		AgentAddress agentAddress = new AgentAddress(targetAgent);
		sitemonitorServerDaemon.sendFile(agentAddress,
			new Directory(tmpDistDirectory.getRootDirectory()),
			Pattern.compile(ConsoleProperties.DEFAULT_DISTRIBUTION_FILE_FILTER_EXPRESSION),
			null);
		sitemonitorServerDaemon.sendToAddressedAgents(agentAddress, new RegistScheduleMessage(
			sitemonitorId, script.getFileName(), hosts, param));
		return targetAgent;
	}

	/**
	 * Find agent that minimum use time for script run.
	 * 
	 * @return
	 */
	AgentIdentity getBestTargetAgent() {
		long minUseTime = Long.MAX_VALUE;
		AgentIdentity targetAgent = null;
		Set<AgentStatus> allAgents = sitemonitorServerDaemon.getAllAgentStatus();
		for (AgentStatus agentStatus : allAgents) {
			long useTime = agentStatus.getMaxUseTimeMilisec();
			if (useTime < minUseTime) {
				minUseTime = useTime;
				targetAgent = agentStatus.getAgentIdentity();
			}
		}
		return targetAgent;
	}

	/**
	 * Prepare script file in svn.
	 * 
	 * @param user
	 * @param sitemonitorId
	 * @param script
	 * @return
	 */
	private SitemonitorDistDirectory prepareDistributeFile(User user, String sitemonitorId,
			FileEntry script) {
		Home home = config.getHome();
		SitemonitorDistDirectory tmpDist = home.createTempSitemonitorDistDirectory(sitemonitorId);
		ProcessingResultPrintStream processingResult = new ProcessingResultPrintStream(
			new ByteArrayOutputStream());
		ScriptHandler handler = scriptHandlerFactory.getHandler(script);
		handler.prepareDistWithRevsion(-0l, user, script, tmpDist.getScriptDirectory(),
			config.getControllerProperties(), processingResult);
		checkTrue(processingResult.isSuccess(), "Failed " + script.getFileName()
			+ " script file prepare.");
		return tmpDist;
	}

}