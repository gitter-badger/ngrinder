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
package org.ngrinder.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractMultiGrinderTestBase;
import org.ngrinder.common.util.ThreadUtils;


/**
 * @author Gisoo Gwon
 */
public class AgentStateMonitorImplementationTest extends AbstractMultiGrinderTestBase {
	
	AgentStateMonitorImplementation sut;
	
	@Before
	public void before() {
		sut = new AgentStateMonitorImplementation(agentConfig1);
	}
	
	@Test
	public void testRecord() throws Exception {
		double min = 5;
		double mid = 10;
		double max = 20;
		
		sut.recordCpuUsePer(mid);
		sut.recordFreeMemory(mid);
		sut.recordUseTime((long) mid);
		
		sut.recordCpuUsePer(max);
		sut.recordFreeMemory(max);
		sut.recordUseTime((long) max);
		
		sut.recordCpuUsePer(min);
		sut.recordFreeMemory(min);
		sut.recordUseTime((long) min);
		
		assertThat(sut.getMaxCpuUsePer(), is(max));
		assertThat(sut.getMinFreeMemory(), is(min));
		assertThat(sut.getMaxUseTimeMilisec(), is((long) max));
	}
	
	@Test
	public void testClear() throws Exception {
		double max = Long.MAX_VALUE;
		
		sut.recordCpuUsePer(max);
		sut.recordFreeMemory(max);
		sut.recordUseTime((long) max);
		
		sut.clear();
		
		assertThat(sut.getMaxCpuUsePer(), greaterThan(0.0));
		assertThat(sut.getMaxCpuUsePer(), lessThan(max));
		assertThat(sut.getMinFreeMemory(), greaterThan(0.0));
		assertThat(sut.getMinFreeMemory(), lessThan(max));
		assertThat(sut.getMaxUseTimeMilisec(), is(0l));
	}
	
	@Test
	public void testCollectorThread() throws Exception {
		assertThat(sut.getMaxCpuUsePer(), is(0.0));
		assertThat(sut.getMinFreeMemory(), is(Double.MAX_VALUE));
		ThreadUtils.sleep(3000);
		
		
		assertThat(sut.getMaxCpuUsePer(), greaterThan(0.0));
		assertThat(sut.getMinFreeMemory(), lessThan(Double.MAX_VALUE));
	}
	
}
