package org.ngrinder.sitemonitor.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ngrinder.model.Sitemonitoring;
import org.ngrinder.model.User;
import org.ngrinder.sitemonitor.repository.SitemonitoringRepository;

import net.grinder.engine.controller.AgentControllerIdentityImplementation;

@RunWith(MockitoJUnitRunner.class)
public class SitemonitoringServiceTest {
	
	@Mock
	SitemonitorManagerService sitemonitorManagerService;

	@Mock
	SitemonitoringRepository sitemonitoringRepository;
	
	@InjectMocks
	SitemonitoringService sut = new SitemonitoringService();

	@Test
		public void testGetSitemonitoringsOf() throws Exception {
			// given
			String runningAgentName = "run";
			String stoppedAgentName = "stop";
			Sitemonitoring runSitemonitoring = new Sitemonitoring();
			Sitemonitoring stopSitemonitoring = new Sitemonitoring();
			runSitemonitoring.setAgentName(runningAgentName);
			stopSitemonitoring.setAgentName(stoppedAgentName);
			List<Sitemonitoring> monitorings = Arrays.asList(runSitemonitoring, stopSitemonitoring);
			
			when(sitemonitoringRepository.findByRegistUser((User) any())).thenReturn(monitorings);
			when(sitemonitorManagerService.getConnectingAgentIdentity(runningAgentName)).thenReturn(
				new AgentControllerIdentityImplementation(null, null));
			when(sitemonitorManagerService.getConnectingAgentIdentity(stoppedAgentName)).thenReturn(null);
			
			// when
			List<Sitemonitoring> actual = sut.getSitemonitoringsOf(new User());
			
			// then
			for (Sitemonitoring sitemonitoring : actual) {
				if (sitemonitoring.getAgentName().equals(runningAgentName)) {
					assertTrue(sitemonitoring.isAgentRunning());
				} else {
					assertFalse(sitemonitoring.isAgentRunning());
				}
			}
		}
	
}
