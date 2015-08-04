package org.ngrinder.sitemon.repository;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.sitemon.model.SiteMonResult;
import org.ngrinder.sitemon.model.SiteMonResult.SiteMonResultPK;
import org.springframework.beans.factory.annotation.Autowired;

public class SiteMonResultRepositoryTest extends AbstractNGrinderTransactionalTest {
	
	@Autowired
	private SiteMonResultRepository sut;
	
	private List<SiteMonResult> siteMonResults;
	
	private SiteMonResult siteMonResult1;
	private SiteMonResult siteMonResult2;
	private SiteMonResult siteMonResult3;
	
	@Before
	public void before() throws ParseException {
		siteMonResult1 = new SiteMonResult("id1", 1, 1, 1, 1, DateUtils.toDate("2015-08-04 10:52:00"), "log1");
		siteMonResult2 = new SiteMonResult("id2", 2, 2, 2, 2, DateUtils.toDate("2015-08-04 10:53:00"), "log2");
		siteMonResult3 = new SiteMonResult("id3", 3, 3, 3, 3, DateUtils.toDate("2015-08-04 10:54:00"), "log3");
		siteMonResults = Arrays.asList(
			siteMonResult1,
			siteMonResult2,
			siteMonResult3
		);
		
		sut.save(siteMonResults);
	}
	
	@After
	public void after() {
		sut.deleteAll();
	}

	@Test
	public void testSave() throws Exception {
		sut.deleteAll();
		List<SiteMonResult> save = sut.save(siteMonResults);
		
		assertThat(save.size(), is(siteMonResults.size()));
	}
	
	@Test
	public void testFindAll() throws Exception {
		List<SiteMonResult> findAll = sut.findAll();
		
		assertThat(findAll.size(), is(siteMonResults.size()));
	}
	
	@Test
	public void testFindOne() throws Exception {
		SiteMonResultPK pk = new SiteMonResultPK(siteMonResult1.getSiteMonId(),
			siteMonResult1.getTestNumber(), siteMonResult1.getTimestamp());
		SiteMonResult siteMonResult = sut.findOne(pk);
		
		assertEqual(siteMonResult1, siteMonResult);
	}
	
	@Test
	public void testFindAllSpecification() throws Exception {
		String siteMonId = siteMonResult1.getSiteMonId();
		Date timestamp = siteMonResult1.getTimestamp();
		List<SiteMonResult> findAll = sut.findAll(SiteMonResultSpecification.idEqualAndAfterTimeOrderByTime(
			siteMonId, timestamp));
		
		assertThat(findAll.size(), is(0));
		
		findAll = sut.findAll(SiteMonResultSpecification.idEqualAndAfterTimeOrderByTime(
			siteMonId, DateUtils.addDay(timestamp, -1)));
		
		assertThat(findAll.size(), is(1));
	}

	private void assertEqual(SiteMonResult res1, SiteMonResult res2) {
		assertThat(res1.getSiteMonId(), is(res2.getSiteMonId()));
		assertThat(res1.getTestNumber(), is(res2.getTestNumber()));
		assertThat(res1.getTimestamp(), is(res2.getTimestamp()));
	}
	
}
