package org.ngrinder.sitemonitor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.model.ScriptType;

/**
 * @author Gisoo Gwon
 */
public class MonitorSchedulerImplementationTest {
	
	private static final int PROCESS_LOAD_WAIT_TIME = 2000;
	
	private MonitorSchedulerImplementation scheduler;
	private File baseDirectory = new File(getClass().getResource("/").getFile());
	private SitemonitorSetting testSetting = new SitemonitorSetting("test1", ScriptType.PYTHON, "", 2000);
	
	@Before
	public void before() throws Exception {
		scheduler = new MonitorSchedulerImplementation(baseDirectory);
		Thread.sleep(500);	// wait for initialize.
	}

	@Test
	public void testStartProcess() throws Exception {
		assertThat(scheduler.getRunningGroups().size(), is(0));
		
		scheduler.startProcess(testSetting);
		Thread.sleep(PROCESS_LOAD_WAIT_TIME);
		assertThat(scheduler.getRunningGroups().size(), is(1));
		
		scheduler.startProcess(testSetting);
		Thread.sleep(PROCESS_LOAD_WAIT_TIME);
		assertThat(scheduler.getRunningGroups().size(), is(1));
	}
	
	@After
	public void after() {
		scheduler.shutdown();
	}
}
