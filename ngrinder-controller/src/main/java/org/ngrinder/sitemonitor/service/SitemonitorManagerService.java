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
import static org.ngrinder.common.util.TypeConvertUtils.*;

import java.util.LinkedList;
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
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ProcessingResultPrintStream;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.sitemonitor.SitemonitorControllerServerDaemon;
import org.ngrinder.sitemonitor.messages.RegistScheduleMessage;
import org.ngrinder.sitemonitor.model.SitemonitorDistDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
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
	private SitemonitorControllerServerDaemon sitemonitorServerDaemon;

	@Autowired
	private Config config;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

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

	/**
	 * @return connecting agent list.
	 */
	public Set<AgentIdentity> getAllAgents() {
		return sitemonitorServerDaemon.getAllAvailableAgents();
	}

	public boolean addSitemonitoring(User user, String sitemonitorId, FileEntry script
		, String hosts, String param) throws FileContentsException, DirectoryException {
		SitemonitorDistDirectory tmpDistDirectory = null;
		try {
			tmpDistDirectory = prepareDistributeFile(user, sitemonitorId, script);

			Set<AgentIdentity> allAgents = sitemonitorServerDaemon.getAllAvailableAgents();
			if (allAgents.size() == 0) {
				return false;
			}

			AgentIdentity targetAgent = allAgents.iterator().next();
			AgentAddress agentAddress = new AgentAddress(targetAgent);
			sitemonitorServerDaemon.sendFile(agentAddress,
				new Directory(tmpDistDirectory.getRootDirectory()),
				Pattern.compile(ConsoleProperties.DEFAULT_DISTRIBUTION_FILE_FILTER_EXPRESSION),
				null);
			sitemonitorServerDaemon.sendToAddressedAgents(agentAddress, new RegistScheduleMessage(
				sitemonitorId, script.getFileName(), hosts, param));
		} finally {
			FileUtils.deleteQuietly(tmpDistDirectory.getRootDirectory());
		}

		return true;
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
		handler.prepareDist(-0l, user, script, tmpDist.getScriptDirectory(),
			config.getControllerProperties(), processingResult);
		checkTrue(processingResult.isSuccess(), "Failed " + script.getFileName()
			+ " script file prepare.");
		return tmpDist;
	}

	/**
	 * @return
	 */
	public List<AgentInfo> getAllAgentInfo() {
		Set<AgentControllerIdentityImplementation> agents = cast(sitemonitorServerDaemon.getAllAvailableAgents());

		List<AgentInfo> agentInfos = new LinkedList<AgentInfo>();
		for (AgentControllerIdentityImplementation agent : agents) {
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setIp(agent.getIp());
			agentInfo.setName(agent.getName());
			agentInfo.setPort(sitemonitorServerDaemon.getAgentConnectingPort(agent));
			agentInfo.setVersion(sitemonitorServerDaemon.getAgentVersion(agent));
			agentInfo.setState(sitemonitorServerDaemon.getAgentState(agent));
			agentInfos.add(agentInfo);
		}

		return agentInfos;
	}

}