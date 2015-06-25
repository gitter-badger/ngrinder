package org.ngrinder.sitemonitor.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.Test;
import org.ngrinder.sitemonitor.SitemonitorControllerServerDaemon;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;

import com.beust.jcommander.internal.Sets;

public class SitemonitorManagerServiceTest {

	SitemonitorManagerService sut;
	Set<AgentIdentity> allAgents;
	String agentName1 = "name1";
	String agentName2 = "name2";
	
	@Test
	public void testIsRunningAgent() throws Exception {
		sut = new SitemonitorManagerService();
		sut.sitemonitorServerDaemon = mock(SitemonitorControllerServerDaemon.class);
		allAgents = Sets.newHashSet();
		allAgents.add(new AgentControllerIdentityImplementation(agentName1, "ip1"));
		allAgents.add(new AgentControllerIdentityImplementation(agentName2, "ip2"));
		when(sut.sitemonitorServerDaemon.getAllAvailableAgents()).thenReturn(allAgents);
		
		assertTrue(sut.isRunningAgent(agentName1));
		assertTrue(sut.isRunningAgent(agentName2));
		assertFalse(sut.isRunningAgent("Unknown"));
	}

}
