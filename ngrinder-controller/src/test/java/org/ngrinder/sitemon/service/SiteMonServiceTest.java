package org.ngrinder.sitemon.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.model.SiteMon;
import org.ngrinder.model.User;
import org.ngrinder.sitemon.model.SiteMonResult;
import org.ngrinder.sitemon.repository.SiteMonRepository;
import org.ngrinder.sitemon.repository.SiteMonResultRepository;
import org.springframework.data.jpa.domain.Specification;

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

	@Mock
	SiteMonResultRepository siteMonResultRepository;
	
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetGraphDataRecentDay() throws Exception {
		// given
		String siteMonId = "id";
		int testNum1 = 1;
		int testNum2 = 2;
		Date timestamp = DateUtils.toDate("2015-07-30 12:10:50");
		List<Integer> testNumbers = Arrays.asList(1, 2, 3);	// 3 is nohave data.
		List<SiteMonResult> resultData = Arrays.asList(
			new SiteMonResult(siteMonId, testNum1, 1, 5, 9, timestamp, null),
			new SiteMonResult(siteMonId, testNum1, 2, 4, 8, timestamp, null),
			new SiteMonResult(siteMonId, testNum1, 3, 3, 7, timestamp, null),
			new SiteMonResult(siteMonId, testNum2, 4, 2, 6, timestamp, null),
			new SiteMonResult(siteMonId, testNum2, 5, 1, 5, timestamp, null));
		when(siteMonResultRepository.findTestNumber(eq(siteMonId), (Date) anyObject())).thenReturn(testNumbers);
		when(siteMonResultRepository.findAll((Specification<SiteMonResult>) anyObject())).thenReturn(resultData);
		
		// when
		Map<String, Object> resultMap = sut.getGraphDataRecentDay(siteMonId);
		
		// then
		List<Integer> labels = (List<Integer>) resultMap.get("labels");
		List<Integer> successData = (List<Integer>) resultMap.get("successData");
		List<Integer> errorData = (List<Integer>) resultMap.get("errorData");
		List<Integer> testTimeData = (List<Integer>) resultMap.get("testTimeData");
		assertThat(labels.toString(), is("[1, 2]"));
		assertThat(
			successData.toString(),
			is("[[[2015-07-30 12:10:50, 1], [2015-07-30 12:10:50, 2], [2015-07-30 12:10:50, 3]], [[2015-07-30 12:10:50, 4], [2015-07-30 12:10:50, 5]]]"));
		assertThat(
			errorData.toString(),
			is("[[[2015-07-30 12:10:50, 5], [2015-07-30 12:10:50, 4], [2015-07-30 12:10:50, 3]], [[2015-07-30 12:10:50, 2], [2015-07-30 12:10:50, 1]]]"));
		assertThat(
			testTimeData.toString(),
			is("[[[2015-07-30 12:10:50, 9], [2015-07-30 12:10:50, 8], [2015-07-30 12:10:50, 7]], [[2015-07-30 12:10:50, 6], [2015-07-30 12:10:50, 5]]]"));
	}
	
}
