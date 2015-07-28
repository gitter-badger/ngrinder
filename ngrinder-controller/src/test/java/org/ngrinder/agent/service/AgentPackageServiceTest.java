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
package org.ngrinder.agent.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.fest.util.Arrays;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.FileUtils;
import org.ngrinder.infra.config.Config;

@RunWith(MockitoJUnitRunner.class)
public class AgentPackageServiceTest {
	@Mock
	Config config;
	
	@InjectMocks
	AgentPackageService sut = new AgentPackageService();
	
	File testFolder = new File("testFolder");
	
	@After
	public void after() {
		org.apache.commons.io.FileUtils.deleteQuietly(testFolder);
	}
	
	@Test
		public void testCreateSiteMonAgentPackage() throws Exception {
			// given
			String connectionIP = "1.2.3.4";
			int port = 12345;
			String version = "3.4";
			String owner = "me";
			Home home = mock(Home.class);
			URLClassLoader ngrinderShClassLoader = loadTargetFolderContainClassLoader();
			
			when(config.getHome()).thenReturn(home);
			when(config.getVersion()).thenReturn(version);
			when(home.getSubFile("download")).thenReturn(testFolder);
			
			
			// when
			File siteMonAgentPackage = sut.createSiteMonAgentPackage(ngrinderShClassLoader, connectionIP, port, owner);
	
			// then
			assertThat(siteMonAgentPackage.exists(), is(true));
			assertThat(siteMonAgentPackage.getName(), is("ngrinder-sitemon-" + version + "-" + connectionIP + "-me.tar"));
			assertTarFiles(siteMonAgentPackage);
		}

	/**
	 * @return The ClassLoader load the target folder, Contain current ClassLoader urls  
	 * @throws MalformedURLException
	 */
	private URLClassLoader loadTargetFolderContainClassLoader() throws MalformedURLException {
		List<File> allFileInTarget = FileUtils.listContainSubFolder(new File("target"), new LinkedList<File>());
				
		URLClassLoader originClassLoader = (URLClassLoader) getClass().getClassLoader();
		URL[] origin = originClassLoader.getURLs();
		
		URL[] newUrls = copyAndAppend(allFileInTarget, origin);
		URLClassLoader ngrinderShClassLoader = new URLClassLoader(newUrls);
		
		return ngrinderShClassLoader;
	}

	private URL[] copyAndAppend(List<File> addFiles, URL[] origin) throws MalformedURLException {
		URL[] newUrls = Arrays.copyOf(origin, origin.length + addFiles.size());
		
		for (int i = 0; i < addFiles.size(); i++) {
			newUrls[origin.length + i] = addFiles.get(i).toURI().toURL();
		}
		
		return newUrls;
	}

	private void assertTarFiles(File siteMonAgentPackage) throws Exception {
		TarArchiveInputStream siteMonAgentTar = null;
		Map<String, Boolean> existFileFlag = new HashMap<String, Boolean>();
		
		try {
			siteMonAgentTar = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(siteMonAgentPackage)));
			ArchiveEntry entry;
			
			while ((entry = siteMonAgentTar.getNextEntry()) != null) {
				existFileFlag.put(entry.getName(), true);
			}
		} finally {
			siteMonAgentTar.close();
		}
		
		assertTrue(existFileFlag.get("ngrinder-sitemon/"));
		assertTrue(existFileFlag.get("ngrinder-sitemon/lib/"));
		assertTrue(existFileFlag.get("ngrinder-sitemon/__agent.conf"));
		assertTrue(existFileFlag.get("ngrinder-sitemon/run_sitemon.bat"));
		assertTrue(existFileFlag.get("ngrinder-sitemon/run_sitemon.sh"));
		assertTrue(existFileFlag.get("ngrinder-sitemon/run_sitemon_bg.sh"));
		assertTrue(existFileFlag.get("ngrinder-sitemon/stop_sitemon.bat"));
		assertTrue(existFileFlag.get("ngrinder-sitemon/stop_sitemon.sh"));
	}
}
