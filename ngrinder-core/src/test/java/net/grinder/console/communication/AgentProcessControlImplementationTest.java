/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.grinder.console.communication;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.util.AgentStateMonitor;

import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerProcessReportMessage;

/**
 * @author Gisoo Gwon
 */
public class AgentProcessControlImplementationTest {
	final long SEC = 1000;
	AgentStateMonitor agentStatusMonitor;
	AgentStatus sut;
	
	@Before
	public void before() {
		agentStatusMonitor = mock(AgentStateMonitor.class);
		AgentControllerProcessReportMessage message = mock(AgentControllerProcessReportMessage.class);
		when(message.getAgentStateMonitor()).thenReturn(agentStatusMonitor);
		sut = mock(AgentProcessControlImplementation.class).new AgentStatus(
			new AgentControllerIdentityImplementation("agentName", "ip"));
		sut.setAgentProcessStatus(message);
	}

	@Test
	public void testGuessMoreRunnableScriptCount() throws Exception {
		// when, 1 script use 1 sec.
		when(agentStatusMonitor.getMaxUseTimeMilisec()).thenReturn(3 * SEC);
		when(agentStatusMonitor.getRepeatInterval()).thenReturn(10 * SEC);
		when(agentStatusMonitor.getRegistScriptCount()).thenReturn(3);
		
		// then
		assertThat(sut.guessMoreRunnableScriptCount(), is(7));
	}

	@Test
	public void testGuessMoreRunnableScriptCount2() throws Exception {
		// when, 1 script use 5 sec.
		when(agentStatusMonitor.getMaxUseTimeMilisec()).thenReturn(5 * SEC);
		when(agentStatusMonitor.getRepeatInterval()).thenReturn(10 * SEC);
		when(agentStatusMonitor.getRegistScriptCount()).thenReturn(1);
		
		// then
		assertThat(sut.guessMoreRunnableScriptCount(), is(1));
	}
	
	@Test
	public void testGuessMoreRunnableScriptCountNoRegisttedScript() throws Exception {
		when(agentStatusMonitor.getRegistScriptCount()).thenReturn(0);
		
		// then
		assertThat(sut.guessMoreRunnableScriptCount(), is(AgentStatus.DEFAULT_GUESS_COUNT));
	}
	
	@Test
	public void testGuessMoreRunnableScriptCountCantGuessState() throws Exception {
		when(agentStatusMonitor.getRegistScriptCount()).thenReturn(5);
		when(agentStatusMonitor.getMaxUseTimeMilisec()).thenReturn(0l);
		
		// then
		assertThat(sut.guessMoreRunnableScriptCount(), is(0));
	}
	
}
