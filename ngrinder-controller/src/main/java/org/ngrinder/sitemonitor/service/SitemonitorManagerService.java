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

import java.util.List;
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
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.sitemonitor.SitemonitorControllerServerDaemon;
import org.ngrinder.sitemonitor.messages.RegistScheduleMessage;
import org.ngrinder.sitemonitor.messages.SitemonitoringReloadMessage;
import org.ngrinder.sitemonitor.messages.UnregistScheduleMessage;
import org.ngrinder.sitemonitor.model.SitemonitorDistDirectory;
import org.ngrinder.sitemonitor.repository.SitemonitoringRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.MessageDispatchRegistry.Handler;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.console.communication.ConsoleCommunicationImplementationEx;
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
	private FileEntryService fileEntryService;
	
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
		registMessage();
	}

	private void registMessage() {
		Handler<SitemonitoringReloadMessage> handler = new Handler<SitemonitoringReloadMessage>() {
			@Override
			public void handle(final SitemonitoringReloadMessage message) throws CommunicationException {
				new Thread(new Runnable() {
					@Override
					public void run() {
						initSitemonitoring(message.getAgentAddress().getIdentity());
					}
				}).start();
			}

			@Override
			public void shutdown() {
			}
		};
		sitemonitorServerDaemon.getComponent(ConsoleCommunicationImplementationEx.class)
			.getMessageDispatchRegistry().set(SitemonitoringReloadMessage.class, handler);
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
	public AgentIdentity getConnectingAgentIdentity(String agentName) {
		for (AgentIdentity identity : getAllAgents()) {
			if (identity.getName().equals(agentName)) {
				return identity;
			}
		}
		return null;
	}

	/**
	 * @return connecting agent list.
	 */
	private Set<AgentIdentity> getAllAgents() {
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
	public String addSitemonitoring(Sitemonitoring sitemonitoring) throws FileContentsException, DirectoryException {
		Set<AgentIdentity> allAgents = sitemonitorServerDaemon.getAllAvailableAgents();
		if (allAgents.size() == 0) {
			return "no have agent";
		}
		
		FileEntry script = fileEntryService.getOne(sitemonitoring.getRegistUser(), sitemonitoring.getScriptName(),
			sitemonitoring.getScriptRevision());
		checkNotNull(script, "Script file '%s' does not exist", script.getFileName());
		
		unregistSitemonitoring(sitemonitoring.getId());
		SitemonitorDistDirectory tmpDistDir = prepareDistributeFile(sitemonitoring, script);
		registSitemonitoringToAgent(sitemonitoring, script, tmpDistDir);
		sitemonitoringRepository.save(sitemonitoring);
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
		checkTrue(sitemonitoring.getRegistUser().getId().equals(user.getId()), 
			"No have grant user {}" + user.getUserName());
		
		unregistSitemonitoring(sitemonitoringId);
	}

	private void unregistSitemonitoring(String sitemonitoringId) {
		Sitemonitoring sitemonitoring = sitemonitoringRepository.findOne(sitemonitoringId);
		if (sitemonitoring != null) {
			AgentIdentity agentIdentity = getConnectingAgentIdentity(sitemonitoring.getAgentName());
			if (agentIdentity != null) {
				AgentAddress agentAddress = new AgentAddress(agentIdentity);
				sitemonitorServerDaemon.sendToAddressedAgents(agentAddress,
					new UnregistScheduleMessage(sitemonitoring.getId(), sitemonitoring.getScriptName()));
			}
			sitemonitoringRepository.delete(sitemonitoringId);
		}
	}

	private void registSitemonitoringToAgent(Sitemonitoring sitemonitoring, FileEntry script,
			SitemonitorDistDirectory tmpDistDirectory)
				throws FileContentsException, DirectoryException {
		AgentAddress agentAddress = new AgentAddress(
			getConnectingAgentIdentity(sitemonitoring.getAgentName()));
		sitemonitorServerDaemon.sendFile(agentAddress,
			new Directory(tmpDistDirectory.getRootDirectory()),
			Pattern.compile(ConsoleProperties.DEFAULT_DISTRIBUTION_FILE_FILTER_EXPRESSION),
			null);
		sitemonitorServerDaemon.sendToAddressedAgents(agentAddress, new RegistScheduleMessage(
			sitemonitoring.getId(), script.getFileName(), sitemonitoring.getTargetHosts(),
			sitemonitoring.getParam()));
	}

	/**
	 * find agent that minimum use time for script run.
	 * @param agentName 
	 * 
	 * @return
	 */
	public String getIdleResouceAgentName() {
		Set<AgentStatus> allAgents = sitemonitorServerDaemon.getAllAgentStatus();
		checkTrue(allAgents.size() > 0, "No have available agent.");
		int maxIdle = 0;
		String targetAgent = null;
		String planBTargetAgent = allAgents.iterator().next().getAgentIdentity().getName();
		for (AgentStatus agentStatus : allAgents) {
			int idle = agentStatus.guessMoreRunnableScriptCount();
			if (maxIdle < idle) {
				maxIdle = idle;
				targetAgent = agentStatus.getAgentIdentity().getName();
			}
		}
		return targetAgent != null ? targetAgent : planBTargetAgent;
	}

	/**
	 * Prepare script file in svn.
	 * 
	 * @param user
	 * @param sitemonitorId
	 * @param script
	 * @return
	 */
	private SitemonitorDistDirectory prepareDistributeFile(Sitemonitoring sitemonitoring,
			FileEntry script) {
		Home home = config.getHome();
		SitemonitorDistDirectory tmpDist = home.createTempSitemonitorDistDirectory(sitemonitoring.getId());
		ProcessingResultPrintStream processingResult = new ProcessingResultPrintStream(
			new ByteArrayOutputStream());
		ScriptHandler handler = scriptHandlerFactory.getHandler(script);
		handler.prepareDistWithRevsion(-0l, sitemonitoring.getRegistUser(), script,
			tmpDist.getScriptDirectory(), config.getControllerProperties(), processingResult);
		checkTrue(processingResult.isSuccess(), "Failed " + script.getFileName()
			+ " script file prepare.");
		return tmpDist;
	}
	
	private void initSitemonitoring(AgentIdentity identity)  {
		List<Sitemonitoring> monitors = sitemonitoringRepository.findByAgentName(identity.getName());
		for (Sitemonitoring monitor : monitors) {
			try {
				addSitemonitoring(monitor);
			} catch (Exception e) {
				LOGGER.error("Failed reload sitemonitoring setting. from agent {}",
					identity.getName());
			}
		}
	}

}