package org.ngrinder.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Gisoo Gwon
 */
public class PerfTestTest {

	@Test
	public void testGetSiteMonScriptName() throws Exception {
		long revision = 10L;
		PerfTest perfTest = new PerfTest();
		perfTest.setScriptName("test/path/script.py");
		perfTest.setScriptRevision(revision);
		
		assertThat(perfTest.getSiteMonScriptName(), is("test/path/script"
			+ PerfTest.SITEMON_TOKEN + revision + ".py"));
	}
	
}
