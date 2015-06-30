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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.grinder.communication.CommunicationException;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.Handler;
import net.grinder.console.communication.ConsoleCommunicationImplementationEx;
import net.grinder.messages.console.ReportStatisticsMessage;

public class SitemonitorScriptRunnerTest {
	private int count;
	SitemonitorScriptRunner sitemonitorScriptRunner;
	File scriptDir = new File(getClass().getResource("/").getFile());

	@Before
	public void before() throws Exception {
		sitemonitorScriptRunner = new SitemonitorScriptRunner(scriptDir);
		Thread.sleep(1000);
	}

	@After
	public void after() throws Exception {
		sitemonitorScriptRunner.shutdown();
	}

	@Test
	public void testRun() throws Exception {
		// monitorId use to script file path in SitemonitorScriptRunner.
		// "." mean current folder. (ex, ~/BASE_DIR/./SCRIPT_FILE) 
		String monitorId = ".";
		count = 0;
		countingReportMessage();
		
		sitemonitorScriptRunner.runWorker(monitorId, "sitemonitor.py", null, null);

		assertThat(count, is(1));
	}

	private void countingReportMessage() {
		ConsoleCommunicationImplementationEx console = sitemonitorScriptRunner.scriptProcessConsole
			.getComponent(ConsoleCommunicationImplementationEx.class);
		MessageDispatchRegistry messageDispatchRegistry = console.getMessageDispatchRegistry();
		messageDispatchRegistry.set(ReportStatisticsMessage.class,
			new Handler<ReportStatisticsMessage>() {
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
