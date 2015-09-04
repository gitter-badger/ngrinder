package org.ngrinder.sitemon.service;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsString;
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
import org.ngrinder.sitemon.model.SiteMonResult;
import org.ngrinder.sitemon.model.SiteMonResult.SiteMonResultPK;
import org.ngrinder.sitemon.repository.SiteMonResultRepository;

/**
 * @author Gisoo Gwon
 */
@RunWith(MockitoJUnitRunner.class)
public class SiteMonResultServiceTest {
	
	@Mock
	SiteMonResultRepository siteMonResultRepository;
	
	@InjectMocks
	SiteMonResultService sut = new SiteMonResultService();
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetGraphData() throws Exception {
		// given
		String siteMonId = "id";
		int testNum1 = 1;
		int testNum2 = 2;
		Date today = DateUtils.toDate("2015-07-30 00:00:00");
		List<SiteMonResult> resultData = Arrays.asList(
			new SiteMonResult(siteMonId, testNum1, 
				"['2015-07-30 12:00:00',3],['2015-07-30 12:01:00',2]", 
				"['2015-07-30 12:00:00',0],['2015-07-30 12:01:00',1]", 
				"['2015-07-30 12:00:00',37],['2015-07-30 12:01:00',42]", today),
			new SiteMonResult(siteMonId, testNum2, 
				"['2015-07-30 12:00:00',1],['2015-07-30 12:01:00',0]", 
				"['2015-07-30 12:00:00',0],['2015-07-30 12:01:00',1]", 
				"['2015-07-30 12:00:00',5],['2015-07-30 12:01:00',9]", today));
		when(siteMonResultRepository.findDistinctTestNumberOrderByTestNumber(siteMonId, today)).thenReturn(
			Arrays.asList(testNum1, testNum2));
		when(
			siteMonResultRepository.findAllBySiteMonIdEqualAndTimestampEqualOrderByTestNumber(
				siteMonId, today)).thenReturn(resultData);
		
		// when
		Map<String, Object> resultMap = sut.getGraphData(siteMonId, today);
		
		// then
		List<Integer> labels = (List<Integer>) resultMap.get("labels");
		String successData = (String) resultMap.get("successData");
		String errorData = (String) resultMap.get("errorData");
		String testTimeData = (String) resultMap.get("testTimeData");
		assertThat(labels.toString(), is("[1, 2]"));
		assertThat(
			successData,
			is("[[['2015-07-30 12:00:00',3],['2015-07-30 12:01:00',2]],[['2015-07-30 12:00:00',1],['2015-07-30 12:01:00',0]]]"));
		assertThat(
			errorData,
			is("[[['2015-07-30 12:00:00',0],['2015-07-30 12:01:00',1]],[['2015-07-30 12:00:00',0],['2015-07-30 12:01:00',1]]]"));
		assertThat(
			testTimeData,
			is("[[['2015-07-30 12:00:00',37],['2015-07-30 12:01:00',42]],[['2015-07-30 12:00:00',5],['2015-07-30 12:01:00',9]]]"));
		assertThat((String) resultMap.get("minTimestamp"), is("2015-07-30 00:00:00"));
		assertThat((String) resultMap.get("maxTimestamp"), is("2015-07-30 23:59:59"));
	}
	
	@Test
	public void testAppendResultWithFirstAppend() throws Exception {
		// given
		Date today = DateUtils.toDate("2015-07-30 00:00:00");
		SiteMonResult newResult = new SiteMonResult("id", 0, 
			"['2015-07-30 12:02:00',1]", 
			"['2015-07-30 12:02:00',2]", 
			"['2015-07-30 12:02:00',3]", today);
		
		// when & then
		when(siteMonResultRepository.findOne((SiteMonResultPK) any())).thenReturn(null);
		sut.appendResult(newResult);
		
		// then
		verify(siteMonResultRepository, times(1)).save(newResult);
	}
	
	@Test
	public void testAppendResultHasOldData() throws Exception {
		// given
		Date today = DateUtils.toDate("2015-07-30 00:00:00");
		SiteMonResult oldResult = new SiteMonResult("id", 0, 
			"['2015-07-30 12:00:00',3],['2015-07-30 12:01:00',2]", 
			"['2015-07-30 12:00:00',0],['2015-07-30 12:01:00',1]", 
			"['2015-07-30 12:00:00',37],['2015-07-30 12:01:00',42]", today);
		SiteMonResult newResult = new SiteMonResult("id", 0, 
			"['2015-07-30 12:02:00',1]", 
			"['2015-07-30 12:02:00',2]", 
			"['2015-07-30 12:02:00',3]", today);
		
		// when
		when(siteMonResultRepository.findOne((SiteMonResultPK) any())).thenReturn(oldResult);
		sut.appendResult(newResult);
		
		// then
		assertThat(oldResult.getSuccess(), is(containsString(newResult.getSuccess())));
		assertThat(oldResult.getError(), is(containsString(newResult.getError())));
		assertThat(oldResult.getTestTime(), is(containsString(newResult.getTestTime())));
	}

}
