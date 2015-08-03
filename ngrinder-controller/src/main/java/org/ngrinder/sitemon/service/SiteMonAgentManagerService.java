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

import static org.ngrinder.common.util.NoOp.*;
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
import org.ngrinder.model.SiteMon;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ProcessingResultPrintStream;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.sitemon.SiteMonControllerServerDaemon;
import org.ngrinder.sitemon.messages.RegistScheduleMessage;
import org.ngrinder.sitemon.messages.SiteMonReloadMessage;
import org.ngrinder.sitemon.messages.SiteMonResultMessage;
import org.ngrinder.sitemon.messages.UnregistScheduleMessage;
import org.ngrinder.sitemon.model.SiteMonDistDirectory;
import org.ngrinder.sitemon.repository.SiteMonRepository;
import org.ngrinder.sitemon.repository.SiteMonResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.Handler;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.Directory;
import net.grinder.util.Directory.DirectoryException;
import net.grinder.util.FileContents.FileContentsException;

/**
 * @author Gisoo Gwon
 */
@Component
public class SiteMonAgentManagerService implements ControllerConstants {

	public static final Logger LOGGER = LoggerFactory.getLogger(SiteMonAgentManagerService.class);
	
	SiteMonControllerServerDaemon siteMonServerDaemon;
	
	@Autowired
	private Config config;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	@Autowired
	private FileEntryService fileEntryService;
	
	@Autowired
	private SiteMonRepository siteMonRepository;
	
	@Autowired
	private SiteMonResultRepository siteMonResultRepository;

	/**
	 * Initialize sitemon manager.
	 */
	@PostConstruct
	public void init() {
		int port = config.getSiteMonAgentControllerPort();
		siteMonServerDaemon = new SiteMonControllerServerDaemon(port);
		siteMonServerDaemon.start();
		registMessage();
	}

	private void registMessage() {
		ConsoleCommunication console = siteMonServerDaemon.getComponent(ConsoleCommunication.class);
		MessageDispatchRegistry register = console.getMessageDispatchRegistry();
		register.set(SiteMonReloadMessage.class, new Handler<SiteMonReloadMessage>() {
			@Override
			public void handle(final SiteMonReloadMessage message) throws CommunicationException {
				new Thread(new Runnable() {
					@Override
					public void run() {
						initSiteMon(message.getAgentAddress().getIdentity());
					}
				}).start();
			}

			@Override
			public void shutdown() {
			}
		});
		register.set(SiteMonResultMessage.class, new Handler<SiteMonResultMessage>() {
			@Override
			public void handle(SiteMonResultMessage message) throws CommunicationException {
				siteMonResultRepository.save(message.getResults());
			}

			@Override
			public void shutdown() {
				noOp();
			}
		});
	}

	/**
	 * Shutdown sitemon controller server.
	 */
	@PreDestroy
	public void destroy() {
		siteMonServerDaemon.shutdown();
	}
	
	public Set<AgentStatus> getAllAgentStatus() {
		return siteMonServerDaemon.getAllAgentStatus();
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
		return siteMonServerDaemon.getAllAvailableAgents();
	}

	/**
	 * If exist siteMonId, remove old siteMon.
	 * Distribute script file and resource to agent.
	 * Send run command to agent.
	 * 
	 * @param user regist user
	 * @param siteMonId	sitemon id
	 * @param script		script entity
	 * @param targetHosts	host setting
	 * @param param			system param
	 * @return 
	 * @throws FileContentsException
	 * @throws DirectoryException
	 */
	public String sendRegist(SiteMon siteMon) throws FileContentsException, DirectoryException {
		String agentName = siteMon.getAgentName();
		checkNotNull(agentName, "No setted sitemon agent name.");
		checkNotNull(getConnectingAgentIdentity(agentName), "Not found '{}' agent.", agentName);
		
		FileEntry script = fileEntryService.getOne(siteMon.getCreatedUser(), siteMon.getScriptName(),
			siteMon.getScriptRevision());
		checkNotNull(script, "Script file '%s' does not exist", script.getFileName());
		
		unregistSiteMon(siteMon.getId());
		SiteMonDistDirectory tmpDistDir = prepareDistributeFile(siteMon, script);
		registSiteMonToAgent(siteMon, script, tmpDistDir);
		siteMonRepository.save(siteMon);
		FileUtils.deleteQuietly(tmpDistDir.getRootDirectory());
		return "success";
	}

	/**
	 * Delete SiteMon info in DB.
	 * Send unregist command to target agent. 
	 * 
	 * @param user	request user
	 * @param siteMonId	sitemon id
	 */
	public void delSiteMon(User user, String siteMonId) {
		SiteMon siteMon = siteMonRepository.findOne(siteMonId);
		checkNotNull(siteMon);
		checkTrue(siteMon.getCreatedUser().getId().equals(user.getId()), 
			"No have grant user {}" + user.getUserName());
		
		unregistSiteMon(siteMonId);
	}

	private void unregistSiteMon(String siteMonId) {
		SiteMon siteMon = siteMonRepository.findOne(siteMonId);
		if (siteMon != null) {
			AgentIdentity agentIdentity = getConnectingAgentIdentity(siteMon.getAgentName());
			if (agentIdentity != null) {
				AgentAddress agentAddress = new AgentAddress(agentIdentity);
				siteMonServerDaemon.sendToAddressedAgents(agentAddress,
					new UnregistScheduleMessage(siteMon.getId(), siteMon.getScriptName()));
			}
			siteMonRepository.delete(siteMonId);
		}
	}

	private void registSiteMonToAgent(SiteMon siteMon, FileEntry script,
			SiteMonDistDirectory tmpDistDirectory)
				throws FileContentsException, DirectoryException {
		AgentAddress agentAddress = new AgentAddress(
			getConnectingAgentIdentity(siteMon.getAgentName()));
		siteMonServerDaemon.sendFile(agentAddress,
			new Directory(tmpDistDirectory.getRootDirectory()),
			Pattern.compile(ConsoleProperties.DEFAULT_DISTRIBUTION_FILE_FILTER_EXPRESSION),
			null);
		siteMonServerDaemon.sendToAddressedAgents(agentAddress, new RegistScheduleMessage(
			siteMon.getId(), script.getFileName(), siteMon.getTargetHosts(),
			siteMon.getParam()));
	}

	/**
	 * find agent that minimum use time for script run.
	 * @param agentName 
	 * 
	 * @return
	 */
	public String getIdleResouceAgentName() {
		Set<AgentStatus> allAgents = siteMonServerDaemon.getAllAgentStatus();
		checkTrue(allAgents.size() > 0, "No have available agent.");
		int maxIdle = 0;
		String targetAgent = null;
		String planBTargetAgent = allAgents.iterator().next().getAgentName();
		for (AgentStatus agentStatus : allAgents) {
			int idle = agentStatus.guessMoreRunnableScriptCount();
			if (maxIdle < idle) {
				maxIdle = idle;
				targetAgent = agentStatus.getAgentName();
			}
		}
		return targetAgent != null ? targetAgent : planBTargetAgent;
	}

	/**
	 * Prepare script file in svn.
	 * @param siteMon
	 * @param script
	 * @return
	 */
	private SiteMonDistDirectory prepareDistributeFile(SiteMon siteMon,
			FileEntry script) {
		Home home = config.getHome();
		SiteMonDistDirectory tmpDist = home.createTempSiteMonDistDirectory(siteMon.getId());
		ProcessingResultPrintStream processingResult = new ProcessingResultPrintStream(
			new ByteArrayOutputStream());
		ScriptHandler handler = scriptHandlerFactory.getHandler(script);
		handler.prepareDistWithRevsion(-0l, siteMon.getCreatedUser(), script,
			tmpDist.getScriptDirectory(), config.getControllerProperties(), processingResult);
		checkTrue(processingResult.isSuccess(), "Failed " + script.getFileName()
			+ " script file prepare.");
		return tmpDist;
	}
	
	private void initSiteMon(AgentIdentity identity)  {
		List<SiteMon> monitors = siteMonRepository.findByAgentName(identity.getName());
		for (SiteMon monitor : monitors) {
			try {
				sendRegist(monitor);
			} catch (Exception e) {
				LOGGER.error("Failed reload sitemon setting. from agent {}",
					identity.getName());
			}
		}
	}

}