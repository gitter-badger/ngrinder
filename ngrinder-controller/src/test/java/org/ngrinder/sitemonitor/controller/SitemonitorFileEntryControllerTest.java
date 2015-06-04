package org.ngrinder.sitemonitor.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;


/**
 * 
 * @author Gisoo Gwon
 */
public class SitemonitorFileEntryControllerTest {
	
	SitemonitorFileEntryController sut = new SitemonitorFileEntryController();
	
	@Test
	public void testNotRedirectViewName() throws Exception {
		String absoluteViewName = "/abc/123";
		String relativeViewName = "abc/123";
		
		String replacedAbsolute = sut.replaceRedirectUrl(absoluteViewName);
		String replacedRelative = sut.replaceRedirectUrl(relativeViewName);
		
		assertThat(replacedAbsolute, is(absoluteViewName));
		assertThat(replacedRelative, is(relativeViewName));
	}
	
	@Test
	public void testRedirectViewName() throws Exception {
		String urlPrefix = SitemonitorFileEntryController.URL_PREFIX;
		String absoluteUrl = "redirect:/abc/123";
		String relativeUrl = "redirect:abc/123";
		
		String replacedAbsolute = sut.replaceRedirectUrl(absoluteUrl);
		String replacedRelative = sut.replaceRedirectUrl(relativeUrl);
		
		assertThat(replacedAbsolute, is("redirect:" + urlPrefix + "/abc/123"));
		assertThat(replacedRelative, is("redirect:" + urlPrefix.replace("/", "") + "/abc/123"));
	}
	
}
