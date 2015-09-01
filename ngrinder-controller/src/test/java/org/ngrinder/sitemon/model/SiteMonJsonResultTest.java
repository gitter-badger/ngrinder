package org.ngrinder.sitemon.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.util.DateUtils;

/**
 * @author Gisoo Gwon
 */
public class SiteMonJsonResultTest {
	
	private SiteMonJsonResult sut;
	private int testNumber1 = 1;
	private int testNumber2 = 2;
	private String minTimestampStr = "2015-07-30 12:00:50";
	private String midtimestampStr = "2015-07-30 12:10:50";
	private String maxTimestampStr = "2015-07-30 12:20:50";
	private List<SiteMonResult> siteMonResults;
	
	@Before
	public void before() throws ParseException {
		siteMonResults = Arrays.asList(
			new SiteMonResult(null, testNumber1, 1, 4, 7, DateUtils.toDate(minTimestampStr), null),
			new SiteMonResult(null, testNumber2, 2, 5, 8, DateUtils.toDate(midtimestampStr), null),
			new SiteMonResult(null, testNumber2, 3, 6, 9, DateUtils.toDate(maxTimestampStr), null));
		sut = new SiteMonJsonResult(siteMonResults);
	}

	@Test
	public void testGetJson() throws Exception {
		// when
		List<List<List<Object>>> sucessJsonAll = sut.getSuccessList();
		List<List<List<Object>>> errorJsonAll = sut.getErrorList();
		List<List<List<Object>>> testTimeJsonAll = sut.getTestTimeList();
		
		// then
		assertThat(sucessJsonAll.size(), is(2));
		assertThat(errorJsonAll.size(), is(2));
		assertThat(testTimeJsonAll.size(), is(2));
		
		assertThat(
			sucessJsonAll.toString(),
			is("[[[" + minTimestampStr + ", 1]], [[" + midtimestampStr + ", 2], [" + maxTimestampStr + ", 3]]]"));
		assertThat(
			errorJsonAll.toString(),
			is("[[[" + minTimestampStr + ", 4]], [[" + midtimestampStr + ", 5], [" + maxTimestampStr + ", 6]]]"));
		assertThat(
			testTimeJsonAll.toString(),
			is("[[[" + minTimestampStr + ", 7]], [[" + midtimestampStr + ", 8], [" + maxTimestampStr + ", 9]]]"));
	}
	
	@Test
	public void testGetLabels() throws Exception {
		List<Integer> labels = sut.getLabelsList();
		
		assertThat(labels.size(), is(2));
		assertThat(labels, hasItem(1));
		assertThat(labels, hasItem(2));
	}
	
	@Test
	public void testTemestamp() throws Exception {
		String minTimestamp = sut.getMinTimestamp();
		String maxTimestamp = sut.getMaxTimestamp();
		
		assertThat(minTimestamp, is(minTimestampStr));
		assertThat(maxTimestamp, is(maxTimestampStr));
	}
	
}
