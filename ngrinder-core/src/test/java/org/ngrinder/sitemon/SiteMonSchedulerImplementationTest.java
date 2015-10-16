package org.ngrinder.sitemon;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.sitemon.SiteMonSchedulerImplementation;
import org.ngrinder.sitemon.SiteMonSchedulerImplementation.MonitoringIntervalChecker;
import org.ngrinder.sitemon.messages.RegistScheduleMessage;
import org.ngrinder.util.AgentStateMonitor;

import net.grinder.engine.agent.SiteMonScriptRunner;


/**
 * MonitorSchedulerImplementation Test.
 * 
 * @author Gisoo Gwon
 */
public class SiteMonSchedulerImplementationTest {
	
	private SiteMonSchedulerImplementation sut;
	private SiteMonScriptRunner siteMonScriptRunner;
	private AgentStateMonitor agentStateMonitor;
	
	int lastSetScriptCount;
	long lastRecordUseTime;
	RegistScheduleMessage message = new RegistScheduleMessage("id", 1, "scriptfile", "host:123", "param", null);
	RegistScheduleMessage message2 = new RegistScheduleMessage("id2", 2, "scriptfile", "host:123", "param", null);
	
	@Before
	public void before() {
		siteMonScriptRunner = mock(SiteMonScriptRunner.class);
		agentStateMonitor = mock(AgentStateMonitor.class);
		initStub();
		sut = new SiteMonSchedulerImplementation(siteMonScriptRunner, agentStateMonitor);
	}

	private void initStub() {
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				lastSetScriptCount = (Integer) invocation.getArguments()[0];
				return null;
			}
		}).when(agentStateMonitor).setRegistScriptCount(anyInt());
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				lastRecordUseTime = (Long) invocation.getArguments()[0];
				return null;
			}
		}).when(agentStateMonitor).recordUseTime(anyLong());
	}
	
	@Test
	public void testRegist() throws Exception {
		sut.regist(message);
		
		assertThat(sut.siteMonMap.size(), is(1));
		assertThat(lastSetScriptCount, is(sut.siteMonMap.size()));
		assertThat(sut.siteMonMap, hasEntry(message.getSiteMonId(), message));
		assertThat(sut.siteMonMap, not(hasEntry(message2.getSiteMonId(), message2)));
		
		sut.regist(message2);
		
		assertThat(sut.siteMonMap.size(), is(2));
		assertThat(lastSetScriptCount, is(sut.siteMonMap.size()));
		assertThat(sut.siteMonMap, hasEntry(message.getSiteMonId(), message));
		assertThat(sut.siteMonMap, hasEntry(message2.getSiteMonId(), message2));
	}
	
	@Test
	public void testUnregist() throws Exception {
		InOrder inOrder = inOrder(agentStateMonitor);
		sut.regist(message);
		sut.regist(message2);
		
		sut.unregist(message.getSiteMonId());
		
		assertThat(sut.siteMonMap.size(), is(1));
		assertThat(lastSetScriptCount, is(sut.siteMonMap.size()));
		assertThat(sut.siteMonMap, not(hasEntry(message.getSiteMonId(), message)));
		assertThat(sut.siteMonMap, hasEntry(message2.getSiteMonId(), message2));
		verify(agentStateMonitor, times(1)).clear();
		
		inOrder.verify(agentStateMonitor).clear();
		inOrder.verify(agentStateMonitor).setRegistScriptCount(anyInt());
	}
	
	@Test
	public void testScriptRun() throws Exception {
		final long scriptUseTime = 800;
		long littleTime = 30;
		long repeatTime = 2000;
		sut.setRepeatTime(repeatTime);
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				ThreadUtils.sleep(scriptUseTime);
				return null;
			}
		}).when(siteMonScriptRunner).runWorker(anyString(), anyString(), anyString(), anyString(),
			anyString(), anyLong());
		ThreadUtils.sleep(repeatTime / 2);
		
		sut.regist(message);
		ThreadUtils.sleep(repeatTime);
		
		verify(siteMonScriptRunner, times(1)).runWorker(eq(message.getSiteMonId()),
			eq(message.getScriptname()), eq(message.getPropHosts()), eq(message.getPropParam()),
			eq(message.getErrorCallback()), anyLong());
		
		assertThat(lastRecordUseTime, greaterThan(scriptUseTime - littleTime));
		assertThat(lastRecordUseTime, lessThan(scriptUseTime + littleTime));
	}
	
	@Test
	public void testIntervalCheckerSkip() throws Exception {
		MonitoringIntervalChecker checker = sut.new MonitoringIntervalChecker(1);
		for (int i = 0; i < 10; i++) {
			assertThat(checker.skip(), is(false));
		}

		checker = sut.new MonitoringIntervalChecker(2);
		for (int i = 0; i < 10; i++) {
			assertThat(checker.skip(), is(true));
			assertThat(checker.skip(), is(false));
		}

		checker = sut.new MonitoringIntervalChecker(3);
		for (int i = 0; i < 10; i++) {
			assertThat(checker.skip(), is(true));
			assertThat(checker.skip(), is(true));
			assertThat(checker.skip(), is(false));
		}
	}
	
}
