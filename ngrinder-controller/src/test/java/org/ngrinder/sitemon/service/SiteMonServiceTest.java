package org.ngrinder.sitemon.service;

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
import org.ngrinder.model.SiteMon;
import org.ngrinder.model.User;
import org.ngrinder.sitemon.repository.SiteMonRepository;

import net.grinder.engine.controller.AgentControllerIdentityImplementation;

/**
 * @author Gisoo Gwon
 */
@RunWith(MockitoJUnitRunner.class)
public class SiteMonServiceTest {
	
	@Mock
	SiteMonAgentManagerService siteMonManagerService;

	@Mock
	SiteMonRepository siteMonRepository;
	
	@InjectMocks
	SiteMonService sut = new SiteMonService();

	@Test
		public void testGetOnesOf() throws Exception {
			// given
			String runningAgentName = "run";
			String stoppedAgentName = "stop";
			SiteMon runSiteMon = new SiteMon();
			SiteMon stopSiteMon = new SiteMon();
			runSiteMon.setAgentName(runningAgentName);
			stopSiteMon.setAgentName(stoppedAgentName);
			List<SiteMon> monitorings = Arrays.asList(runSiteMon, stopSiteMon);
			
			when(siteMonRepository.findByCreatedUser((User) any())).thenReturn(monitorings);
			when(siteMonManagerService.getConnectingAgentIdentity(runningAgentName)).thenReturn(
				new AgentControllerIdentityImplementation(null, null));
			when(siteMonManagerService.getConnectingAgentIdentity(stoppedAgentName)).thenReturn(null);
			
			// when
			List<SiteMon> actual = sut.getAll(new User());
			
			// then
			for (SiteMon siteMon : actual) {
				if (siteMon.getAgentName().equals(runningAgentName)) {
					assertTrue(siteMon.isAgentRunning());
				} else {
					assertFalse(siteMon.isAgentRunning());
				}
			}
		}
	
}
