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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.sitemon.SiteMonControllerServerDaemon;
import org.ngrinder.util.AgentStateMonitor;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerProcessReportMessage;
import net.grinder.messages.console.AgentAddress;

import com.beust.jcommander.internal.Sets;

/**
 * @author Gisoo Gwon
 */
public class SiteMonAgentManagerServiceTest {

	SiteMonAgentManagerService sut;
	Set<AgentIdentity> allAgents;
	String agentName1 = "name1";
	String agentName2 = "name2";
	AgentIdentity agent1 = new AgentControllerIdentityImplementation(
		agentName1, "ip1");
	AgentIdentity agent2 = new AgentControllerIdentityImplementation(
		agentName2, "ip2");
	
	@Before
	public void before() {
		sut = new SiteMonAgentManagerService();
		sut.siteMonServerDaemon = mock(SiteMonControllerServerDaemon.class);
	}

	@Test
	public void testGetConnectingAgentIdentity() throws Exception {
		allAgents = Sets.newHashSet();
		allAgents.add(agent1);
		allAgents.add(agent2);
		when(sut.siteMonServerDaemon.getAllAvailableAgents()).thenReturn(allAgents);

		assertNotNull(sut.getConnectingAgentIdentity(agentName1));
		assertNotNull(sut.getConnectingAgentIdentity(agentName2));
		assertNull(sut.getConnectingAgentIdentity("Unknown"));
	}

	@Test
	public void testRegistBestTargetAgent() throws Exception {
		// given
		int big = 10;
		int small = 5;
		AgentStatus status1 = mock(AgentProcessControlImplementation.class).new AgentStatus(agent1);
		AgentStatus status2 = mock(AgentProcessControlImplementation.class).new AgentStatus(agent2);
		AgentControllerProcessReportMessage status1Message = new AgentControllerProcessReportMessage(null, new SystemDataModel(), -1, "");
		AgentControllerProcessReportMessage status2Message = new AgentControllerProcessReportMessage(null, new SystemDataModel(), -1, "");
		status1Message.setAddress(new AgentAddress(agent1));
		status2Message.setAddress(new AgentAddress(agent2));
		status1Message.setAgentStateMonitor(mock(AgentStateMonitor.class));
		status2Message.setAgentStateMonitor(mock(AgentStateMonitor.class));
		status1.setAgentProcessStatus(status1Message);
		status2.setAgentProcessStatus(status2Message);
		
		Set<AgentStatus> agentStatus = new HashSet<AgentStatus>();
		agentStatus.add(status1);
		agentStatus.add(status2);
		
		when(sut.siteMonServerDaemon.getAllAgentStatus()).thenReturn(agentStatus);
		
		// when
		whenGuessMoreRunnableScriptCount(status1Message, big);
		whenGuessMoreRunnableScriptCount(status2Message, small);
		String agentName = sut.getIdleResouceAgentName();
		
		// then
		assertThat(agentName, is(agentName1));
		
		// when
		whenGuessMoreRunnableScriptCount(status1Message, small);
		whenGuessMoreRunnableScriptCount(status2Message, big);
		agentName = sut.getIdleResouceAgentName();
		
		// then
		assertThat(agentName, is(agentName2));
	}

	private void whenGuessMoreRunnableScriptCount(
		AgentControllerProcessReportMessage statusMessage, int returnVal) {
		// (registCount / useTime) * (repeatInterval - useTime) 
		// = (registCount / 1) * (2 - 1) = registCount = returnVal
		when(statusMessage.getAgentStateMonitor().getRegistScriptCount()).thenReturn(returnVal);
		when(statusMessage.getAgentStateMonitor().getRepeatInterval()).thenReturn(2l);
		when(statusMessage.getAgentStateMonitor().getMaxUseTimeMilisec()).thenReturn(1l);
	}

}
