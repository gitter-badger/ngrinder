package org.ngrinder.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Gisoo Gwon
 */
public class PerfTestTest {

	@Test
	public void testGetSitemonitoringScriptName() throws Exception {
		long revision = 10L;
		PerfTest perfTest = new PerfTest();
		perfTest.setScriptName("test/path/script.py");
		perfTest.setScriptRevision(revision);
		
		assertThat(perfTest.getSitemonitoringScriptName(), is("test/path/script"
			+ PerfTest.SITEMONITORING_TOKEN + revision + ".py"));
	}
	
}
