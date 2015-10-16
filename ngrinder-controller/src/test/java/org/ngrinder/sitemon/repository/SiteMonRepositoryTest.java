package org.ngrinder.sitemon.repository;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.SiteMon;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class SiteMonRepositoryTest extends AbstractNGrinderTransactionalTest {
	
	@Autowired
	private SiteMonRepository sut;
	
	@Autowired
	private UserRepository userRepository;
	
	private User user1 = new User("id1", "name1", "pass1", "mail1", Role.ADMIN);
	private User user2 = new User("id2", "name2", "pass2", "mail2", Role.USER);
	private SiteMon siteMon1;
	private SiteMon siteMon2;
	
	@Before
	public void before() {
		user1.setUserId("1");
		user2.setUserId("2");
		userRepository.save(user1);
		userRepository.save(user2);
		
		siteMon1 = new SiteMon("id1", "name1", 1, user1, "script1", 1, "hosts1", "param1", "agent1", true, "back1");
		siteMon2 = new SiteMon("id2", "name2", 2, user2, "script2", 2, "hosts2", "param2", "agent2", false, "back2");
		sut.save(siteMon1);
		sut.save(siteMon2);
	}

	@Test
	public void testSave() throws Exception {
		SiteMon siteMon = new SiteMon("id", "name", 0, new User(), "script", 0, "hosts", "param", "agent", true, "back");
		SiteMon save = sut.save(siteMon);
		assertEqual(siteMon, save);
	}
	
	@Test
	public void testFindOne() throws Exception {
		SiteMon siteMon = sut.findOne(siteMon1.getId());
		
		assertEqual(siteMon1, siteMon);
	}
	
	@Test
	public void testFindCreatedUser() throws Exception {
		List<SiteMon> siteMons = sut.findByCreatedUser(user1);
		
		assertThat(siteMons.size(), is(1));
		assertEqual(siteMon1, siteMons.get(0));
	}
	
	@Test
	public void testFindByRunState() throws Exception {
		List<SiteMon> runList = sut.findByRunState(true);
		List<SiteMon> pauseList = sut.findByRunState(false);
		
		assertThat(runList.size(), is(1));
		assertThat(pauseList.size(), is(1));
		assertEqual(siteMon1, runList.get(0));
		assertEqual(siteMon2, pauseList.get(0));
	}
	
	@Test
	public void testFindByAgentNameAndRunState() throws Exception {
		List<SiteMon> runSiteMons = sut.findByAgentNameAndRunState(siteMon1.getAgentName(), true);
		List<SiteMon> pauseSiteMons = sut.findByAgentNameAndRunState(siteMon1.getAgentName(), false);
		
		assertThat(runSiteMons.size(), is(1));
		assertThat(pauseSiteMons.size(), is(0));
		assertEqual(siteMon1, runSiteMons.get(0));
	}
	
	@Test
	public void testDelete() throws Exception {
		sut.delete(siteMon1.getId());
		
		SiteMon deleted = sut.findOne(siteMon1.getId());
		List<SiteMon> siteMons = sut.findAll();
		
		assertThat(deleted, is(nullValue()));
		assertThat(siteMons.size(), is(1));
		assertEqual(siteMon2, siteMons.get(0));
	}
	
	@Test
	public void testUpdate() throws Exception {
		SiteMon siteMon = sut.findOne(siteMon1.getId());
		siteMon.setAgentName("new");
		SiteMon updated = sut.saveAndFlush(siteMon);
		
		assertThat(updated, is(notNullValue()));
		assertEqual(siteMon, updated);
	}

	private void assertEqual(SiteMon mon1, SiteMon mon2) {
		assertThat(mon2.getId(), is(mon1.getId()));
		assertThat(mon2.getName(), is(mon1.getName()));
		assertThat(mon2.getScriptName(), is(mon1.getScriptName()));
		assertThat(mon2.getScriptRevision(), is(mon1.getScriptRevision()));
		assertThat(mon2.getTargetHosts(), is(mon1.getTargetHosts()));
		assertThat(mon2.getParam(), is(mon1.getParam()));
		assertThat(mon2.getAgentName(), is(mon1.getAgentName()));
		assertThat(mon2.isRunState(), is(mon1.isRunState()));
		assertThat(mon2.getErrorCallback(), is(mon1.getErrorCallback()));
	}
	
}
