package org.ngrinder.sitemon.repository;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.lessThan;
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
	private SiteMonResult siteMonResult4;
	private SiteMonResult siteMonResult5;
	
	@Before
	public void before() throws ParseException {
		sut.deleteAll();
		siteMonResult1 = new SiteMonResult("id1", 1, "['2015-09-03 12:11:11',1]", "", "", DateUtils.toDate("2015-08-04 10:52:00"));
		siteMonResult2 = new SiteMonResult("id2", 1, "", "", "", DateUtils.toDate("2015-08-04 10:52:00"));
		siteMonResult3 = new SiteMonResult("id2", 1, "", "", "", DateUtils.toDate("2015-08-04 10:53:00"));
		siteMonResult4 = new SiteMonResult("id2", 2, "", "", "", DateUtils.toDate("2015-08-05 10:52:00"));
		siteMonResult5 = new SiteMonResult("id2", 1, "", "", "", DateUtils.toDate("2015-08-05 10:52:00"));
		siteMonResults = Arrays.asList(
			siteMonResult1,
			siteMonResult2,
			siteMonResult3,
			siteMonResult4,
			siteMonResult5
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
	public void testSaveAndFlush() throws Exception {
		SiteMonResultPK pk = new SiteMonResultPK(siteMonResult1.getSiteMonId(),
			siteMonResult1.getTestNumber(), siteMonResult1.getTimestamp());
		SiteMonResult findOne = sut.findOne(pk);
		findOne.setSuccess("changed");
		findOne.setError("changed");
		findOne.setTestTime("changed");
		SiteMonResult saveAndFlush = sut.saveAndFlush(findOne);
		
		assertThat(saveAndFlush.getSuccess(), is("changed"));
		assertThat(saveAndFlush.getError(), is("changed"));
		assertThat(saveAndFlush.getTestTime(), is("changed"));
	}
	
	@Test
	public void testFindAllSpecification() throws Exception {
		List<SiteMonResult> findAll = sut.findAllBySiteMonIdEqualAndTimestampEqualOrderByTestNumber(
			siteMonResult1.getSiteMonId(), siteMonResult1.getTimestamp());
		assertThat(findAll.size(), is(1));
		
		findAll = sut.findAllBySiteMonIdEqualAndTimestampEqualOrderByTestNumber(
			siteMonResult1.getSiteMonId(), new Date());
		assertThat(findAll.size(), is(0));
		
		findAll = sut.findAllBySiteMonIdEqualAndTimestampEqualOrderByTestNumber(
			siteMonResult4.getSiteMonId(), siteMonResult4.getTimestamp());
		assertThat(siteMonResult4.getSiteMonId(), is(siteMonResult5.getSiteMonId()));
		assertThat(siteMonResult4.getTimestamp(), is(siteMonResult5.getTimestamp()));
		assertThat(findAll.size(), is(2));
		assertThat(findAll.get(0).getTestNumber(), is(lessThan(findAll.get(1).getTestNumber())));
	}
	
	@Test
	public void testFindDistinctTestNumberOrderByTestNumber() throws Exception {
		List<Integer> id1TestNumbers = sut.findDistinctTestNumberOrderByTestNumber(
			siteMonResult1.getSiteMonId(), siteMonResult1.getTimestamp());
		List<Integer> id2TestNumbers = sut.findDistinctTestNumberOrderByTestNumber(
			siteMonResult2.getSiteMonId(), siteMonResult2.getTimestamp());
		
		assertThat(id1TestNumbers.size(), is(1));
		assertThat(id1TestNumbers, is(hasItems(siteMonResult1.getTestNumber())));
		
		assertThat(id2TestNumbers.size(), is(2));
		assertThat(id2TestNumbers, is(hasItems(siteMonResult2.getTestNumber(), siteMonResult5.getTestNumber())));
	}
	
	@Test
	public void testDeleteBeforeTimestamp() throws Exception {
		int affected = sut.deleteBeforeTimestamp(siteMonResult2.getTimestamp());
		
		assertThat(affected, is(2));
	}

	private void assertEqual(SiteMonResult res1, SiteMonResult res2) {
		assertThat(res1.getSiteMonId(), is(res2.getSiteMonId()));
		assertThat(res1.getTestNumber(), is(res2.getTestNumber()));
		assertThat(res1.getTimestamp(), is(res2.getTimestamp()));
	}
	
}
