package org.ngrinder.sitemonitor;

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
import org.ngrinder.sitemonitor.messages.RegistScheduleMessage;
import org.ngrinder.util.AgentStateMonitor;

import net.grinder.engine.agent.SitemonitorScriptRunner;


/**
 * MonitorSchedulerImplementation Test.
 * 
 * @author Gisoo Gwon
 */
public class MonitorSchedulerImplementationTest {
	
	private MonitorSchedulerImplementation sut;
	private SitemonitorScriptRunner sitemonitorScriptRunner;
	private AgentStateMonitor agentStateMonitor;
	
	int lastSetScriptCount;
	long lastRecordUseTime;
	RegistScheduleMessage message = new RegistScheduleMessage("id", "scriptfile", "host:123", "param,null");
	RegistScheduleMessage message2 = new RegistScheduleMessage("id2", "scriptfile", "host:123", "param,null");
	
	@Before
	public void before() {
		sitemonitorScriptRunner = mock(SitemonitorScriptRunner.class);
		agentStateMonitor = mock(AgentStateMonitor.class);
		initStub();
		sut = new MonitorSchedulerImplementation(sitemonitorScriptRunner, agentStateMonitor);
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
		
		assertThat(sut.sitemonitoringMap.size(), is(1));
		assertThat(lastSetScriptCount, is(sut.sitemonitoringMap.size()));
		assertThat(sut.sitemonitoringMap, hasEntry(message.getSitemonitoringId(), message));
		assertThat(sut.sitemonitoringMap, not(hasEntry(message2.getSitemonitoringId(), message2)));
		
		sut.regist(message2);
		
		assertThat(sut.sitemonitoringMap.size(), is(2));
		assertThat(lastSetScriptCount, is(sut.sitemonitoringMap.size()));
		assertThat(sut.sitemonitoringMap, hasEntry(message.getSitemonitoringId(), message));
		assertThat(sut.sitemonitoringMap, hasEntry(message2.getSitemonitoringId(), message2));
	}
	
	@Test
	public void testUnregist() throws Exception {
		InOrder inOrder = inOrder(agentStateMonitor);
		sut.regist(message);
		sut.regist(message2);
		
		sut.unregist(message.getSitemonitoringId());
		
		assertThat(sut.sitemonitoringMap.size(), is(1));
		assertThat(lastSetScriptCount, is(sut.sitemonitoringMap.size()));
		assertThat(sut.sitemonitoringMap, not(hasEntry(message.getSitemonitoringId(), message)));
		assertThat(sut.sitemonitoringMap, hasEntry(message2.getSitemonitoringId(), message2));
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
		}).when(sitemonitorScriptRunner).runWorker(anyString(), anyString(), anyString(), anyString());
		ThreadUtils.sleep(repeatTime / 2);
		
		sut.regist(message);
		ThreadUtils.sleep(repeatTime);
		
		verify(sitemonitorScriptRunner, times(1)).runWorker(message.getSitemonitoringId(),
			message.getScriptname(), message.getPropHosts(), message.getPropParam());
		
		assertThat(lastRecordUseTime, greaterThan(scriptUseTime - littleTime));
		assertThat(lastRecordUseTime, lessThan(scriptUseTime + littleTime));
	}
	
}
