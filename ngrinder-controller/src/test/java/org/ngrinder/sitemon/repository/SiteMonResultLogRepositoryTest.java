package org.ngrinder.sitemon.repository;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.sitemon.model.SiteMonResultLog;
import org.ngrinder.sitemon.model.SiteMonResultLog.PK;
import org.springframework.beans.factory.annotation.Autowired;

public class SiteMonResultLogRepositoryTest extends AbstractNGrinderTransactionalTest {
	
	@Autowired
	private SiteMonResultLogRepository sut;
	
	@Before
	public void before() {
		sut.deleteAll();
	}
	
	@Test
	public void testFindAllWithEmpty() throws Exception {
		List<SiteMonResultLog> findAll = sut.findAll();
		assertThat(findAll.size(), is(0));
	}
	
	@Test
	public void testSave() throws Exception {
		// given
		SiteMonResultLog log = new SiteMonResultLog("id1", DateUtils.toDate("2015-08-04 10:52:00"),
			"test_log_data");
		
		// when
		sut.save(log);
		List<SiteMonResultLog> findAll = sut.findAll();
		
		// then
		assertThat(findAll.size(), is(1));
		assertThat(findAll.get(0).getSiteMonId(), is(log.getSiteMonId()));
		assertThat(findAll.get(0).getTimestamp(), is(log.getTimestamp()));
		assertThat(findAll.get(0).getLog(), is(log.getLog()));
	}
	
	@Test
	public void testFindOne() throws Exception {
		// given
		PK pk = new PK("id1", DateUtils.toDate("2015-08-04 10:52:00"));
		SiteMonResultLog log = new SiteMonResultLog();
		log.setSiteMonId(pk.getSiteMonId());
		log.setTimestamp(pk.getTimestamp());
		log.setLog("test_log_data");
		
		// when
		sut.save(log);
		SiteMonResultLog findOne = sut.findOne(pk);
		
		// then
		assertThat(findOne.getSiteMonId(), is(pk.getSiteMonId()));
		assertThat(findOne.getTimestamp(), is(pk.getTimestamp()));
		assertThat(findOne.getLog(), is("test_log_data"));
	}
	
	@Test
	public void testDeleteBeforeTimestamp() throws Exception {
		sut.save(Arrays.asList(
			new SiteMonResultLog("id1", DateUtils.toDate("2015-08-04 10:52:00"), "id1 52min"),
			new SiteMonResultLog("id1", DateUtils.toDate("2015-08-04 10:53:00"), "id1 53min"),
			new SiteMonResultLog("id1", DateUtils.toDate("2015-08-04 10:54:00"), "id1 54min"),
			new SiteMonResultLog("id2", DateUtils.toDate("2015-08-04 10:53:00"), "id2 53min")
			));
		
		int affected = sut.deleteBeforeTimestamp(DateUtils.toDate("2015-08-04 10:53:00"));
		
		assertThat(affected, is(3));
		assertThat(sut.findAll().get(0).getLog(), is("id1 54min"));
	}
	
	@Test
	public void testFindErrorLog() throws Exception {
		sut.save(Arrays.asList(
			new SiteMonResultLog("id1", DateUtils.toDate("2015-08-04 10:52:00"), "id1 52min"),
			new SiteMonResultLog("id1", DateUtils.toDate("2015-08-04 10:53:00"), "id1 53min"),
			new SiteMonResultLog("id1", DateUtils.toDate("2015-08-04 10:54:00"), "id1 54min"),
			new SiteMonResultLog("id2", DateUtils.toDate("2015-08-04 10:53:00"), "id2 53min"),
			new SiteMonResultLog("id2", DateUtils.toDate("2015-08-04 10:54:00"), "id2 54min")
			));
		
		List<String> logs = sut.findErrorLog("id1", DateUtils.toDate("2015-08-04 10:52:00"),
			DateUtils.toDate("2015-08-04 10:53:00"));
		
		assertThat(logs.size(), is(2));
		assertThat(logs, is(hasItem("id1 52min")));
		assertThat(logs, is(hasItem("id1 53min")));
	}

}
