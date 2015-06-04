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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.sitemonitor.SitemonitorControllerServerDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.Message;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;

import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * @author Gisoo Gwon
 */
@Component
public class SitemonitorManagerService implements ControllerConstants {

	public static final Logger LOGGER = LoggerFactory.getLogger(SitemonitorManagerService.class);
	private SitemonitorControllerServerDaemon sitemonitorServerDaemon;

	@Autowired
	private Config config;

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
	public Set<AgentIdentity> getAllAgent() {
		return sitemonitorServerDaemon.getAllAvailableAgents();
	}
	
	public void sendToAgents(Message message) {
		sitemonitorServerDaemon.sendToAgents(message);
	}

	/**
	 * @return
	 */
	public List<AgentInfo> getAllAgentInfo() {
		Set<AgentControllerIdentityImplementation> agents
			= cast(sitemonitorServerDaemon.getAllAvailableAgents());
		
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