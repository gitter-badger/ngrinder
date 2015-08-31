package org.ngrinder.sitemon.service;

import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ngrinder.infra.config.Config;
import org.ngrinder.sitemon.repository.SiteMonResultRepository;

/**
 * 
 * @author Gisoo Gwon
 */
@RunWith(MockitoJUnitRunner.class)
public class BatchScheduleServiceTest {
	
	@Mock
	private Config config;
	
	@Mock
	private SiteMonResultRepository siteMonResultRepository;
	
	@InjectMocks
	private BatchScheduleService sut = new BatchScheduleService();

	@Test
	public void testName() throws Exception {
		// given
		String local = "127.0.0.1";
		String anyIp = "220.220.220.220";
		
		// when then
		when(config.getBatchServerIp()).thenReturn(anyIp);
		sut.deleteOldSiteMonResult();
		verify(siteMonResultRepository, times(0)).deleteBeforeTimestamp(any(Date.class));
		verify(config, times(0)).getSiteMonResultMaxHistory();
		
		// when then
		when(config.getBatchServerIp()).thenReturn(local);
		sut.deleteOldSiteMonResult();
		verify(siteMonResultRepository, times(1)).deleteBeforeTimestamp(any(Date.class));
		verify(config, times(1)).getSiteMonResultMaxHistory();
	}
	
}
