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

/**
 * @author Gisoo Gwon
 */
public class SiteMonScriptRunnerTest {
	SiteMonScriptRunner siteMonScriptRunner;
	File scriptDir = new File(getClass().getResource("/").getFile());

	@Before
	public void before() throws Exception {
		siteMonScriptRunner = new SiteMonScriptRunner(scriptDir);
		Thread.sleep(1000);
	}

	@After
	public void after() throws Exception {
		siteMonScriptRunner.shutdown();
	}

	@Test
	public void testRun() throws Exception {
		// monitorId use to script file path in SiteMonScriptRunner.
		// "." mean current folder. (ex, ~/BASE_DIR/./SCRIPT_FILE) 
		String monitorId = ".";
		
		assertThat(siteMonScriptRunner.pollAllResult().size(), is(0));
		
		siteMonScriptRunner.runWorker(monitorId, "sitemon.py", null, null);

		assertThat(siteMonScriptRunner.pollAllResult().size(), is(1));
		assertThat(siteMonScriptRunner.pollAllResult().size(), is(0));
	}
}
