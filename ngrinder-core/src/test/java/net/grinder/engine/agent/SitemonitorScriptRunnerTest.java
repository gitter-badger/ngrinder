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
package net.grinder.engine.agent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.ServerSocket;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.sitemonitor.SitemonitorControllerServerDaemon;
import org.ngrinder.sitemonitor.SitemonitorSetting;
import org.ngrinder.sitemonitor.messages.AddScriptMessage;
import org.ngrinder.sitemonitor.messages.RemoveScriptMessage;

import net.grinder.communication.CommunicationException;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.Handler;
import net.grinder.console.communication.ConsoleCommunicationImplementationEx;
import net.grinder.engine.agent.SitemonitorScriptRunner.ScriptType;
import net.grinder.messages.console.ReportStatisticsMessage;

public class SitemonitorScriptRunnerTest {
	private static final int LITTLE_DELAY = 50;
	private static final int LONG_DELAY = 2000;
	private int count;
	int freePort;
	SitemonitorScriptRunner sitemonitorScriptRunner;
	SitemonitorControllerServerDaemon serverDaemon;
	
	@Before
	public void before() throws Exception {
		ServerSocket socket = new ServerSocket(0);
		int consolePort = socket.getLocalPort();
		IOUtils.closeQuietly(socket);
		
		serverDaemon = new SitemonitorControllerServerDaemon(consolePort);
		serverDaemon.start();
		Thread.sleep(1000);
		
		sitemonitorScriptRunner = new SitemonitorScriptRunner(serverDaemon);
	}
	
	@After
	public void after() throws Exception {
		sitemonitorScriptRunner.shutdown();
	}
	
	@Test
	public void testInitWithThread() throws Exception {
		int repteatCount = 2;
		String groupName = "groupName";
		String errerCallback = "";
		int repeatCycle = 2000;
		SitemonitorSetting setting = new SitemonitorSetting(groupName, errerCallback, repeatCycle);
		
		count = 0;
		registCountReportMessage();
		
		File base = new File(getClass().getResource("/").getFile());
		sitemonitorScriptRunner.initWithThread(setting, base, ScriptType.PYTHON);
		Thread.sleep(1000);
		
		assertThat(count, is(0));
		
		sitemonitorScriptRunner.sendMessage(groupName, new AddScriptMessage(new File(base, "sitemonitor.py").getAbsolutePath()));
		Thread.sleep(repeatCycle * repteatCount + LITTLE_DELAY);
		sitemonitorScriptRunner.sendMessage(groupName, new RemoveScriptMessage(new File(base, "sitemonitor.py").getAbsolutePath()));
		Thread.sleep(LONG_DELAY);
		
		assertThat(count, is(repteatCount));
	}

	private void registCountReportMessage() {
		ConsoleCommunicationImplementationEx console = serverDaemon.getComponent(ConsoleCommunicationImplementationEx.class);
		MessageDispatchRegistry messageDispatchRegistry = console.getMessageDispatchRegistry();
		messageDispatchRegistry.set(ReportStatisticsMessage.class, new Handler<ReportStatisticsMessage>() {
			@Override
			public void handle(ReportStatisticsMessage message) throws CommunicationException {
				count += message.getStatisticsDelta().size();
			}

			@Override
			public void shutdown() {
				
			}
		});
	}
}
