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
package org.ngrinder.script.repository;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.infra.init.DBInit;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FileEntryRepositoryTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	public MockFileEntityRepository repo;

	@Autowired
	public IUserService userService;

	@Autowired
	public DBInit dbinit;

	/**
	 * Locate dumped user1 repo into tempdir
	 *
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {

		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(file);
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
		repo.setUserRepository(new File(file, getTestUser().getUserId()));
	}

	@Test
	public void testFileEntitySaveAndDelete() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent("HELLO WORLD2");
		fileEntry.setEncoding("UTF-8");
		fileEntry.setPath("helloworld.txt");
		fileEntry.setDescription("WOW");
		fileEntry.getProperties().put("hello", "world");

		int size = repo.findAll(getTestUser()).size();
		repo.save(getTestUser(), fileEntry, fileEntry.getEncoding());

		FileEntry findOne = repo.findOne(getTestUser(), "helloworld.txt", SVNRevision.HEAD);
		assertThat(findOne.getProperties().get("hello"), is("world"));

		fileEntry.setPath("www");
		fileEntry.setFileType(FileType.DIR);
		repo.save(getTestUser(), fileEntry, null);
		fileEntry.setPath("www/aa.py");
		fileEntry.setFileType(FileType.PYTHON_SCRIPT);
		repo.save(getTestUser(), fileEntry, "UTF-8");
		assertThat(repo.findAll(getTestUser()).size(), is(size + 3));

		repo.delete(getTestUser(), Lists.newArrayList("helloworld.txt"));
		assertThat(repo.findAll(getTestUser()).size(), is(size + 2));

		// Attempt to create duplicated path
		fileEntry.setPath("www");
		fileEntry.setFileType(FileType.DIR);
		// It should be allowed.
		repo.save(getTestUser(), fileEntry, null);
		fileEntry.setPath("helloworld.txt");

	}

	@Test
	public void testBinarySaveAndLoad() throws IOException {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent("HELLO WORLD2");
		fileEntry.setEncoding("UTF-8");
		fileEntry.setPath("helloworld.txt");
		fileEntry.setFileType(FileType.TXT);
		fileEntry.setDescription("WOW");
		repo.save(getTestUser(), fileEntry, fileEntry.getEncoding());
		fileEntry.setPath("hello.zip");
		fileEntry.setEncoding(null);
		fileEntry.setFileType(FileType.UNKNOWN);
		byte[] byteArray = IOUtils.toByteArray(new ClassPathResource("TEST_USER.zip").getInputStream());
		fileEntry.setContentBytes(byteArray);
		repo.save(getTestUser(), fileEntry, null);
		List<FileEntry> findAll = repo.findAll(getTestUser(), "hello.zip", null);
		FileEntry foundEntry = findAll.get(0);
		assertThat(foundEntry.getFileSize(), is((long) byteArray.length));
		// commit again
		repo.save(getTestUser(), fileEntry, null);
		findAll = repo.findAll(getTestUser(), "hello.zip", null);
		assertThat(foundEntry.getFileSize(), is((long) byteArray.length));
	}

	@Test
	public void testBinarySaveAndLoadWithFindOne() throws IOException {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent("HELLO WORLD2");
		fileEntry.setEncoding("UTF-8");
		fileEntry.setPath("hello.zip");
		fileEntry.setEncoding(null);
		fileEntry.setFileType(FileType.UNKNOWN);
		byte[] byteArray = IOUtils.toByteArray(new ClassPathResource("TEST_USER.zip").getInputStream());
		fileEntry.setContentBytes(byteArray);
		repo.save(getTestUser(), fileEntry, null);
		FileEntry foundEntry = repo.findOne(getTestUser(), "hello.zip", SVNRevision.HEAD);
		assertThat(foundEntry.getFileSize(), is((long) byteArray.length));
	}

	@Test
	public void testNotExistingPath() throws IOException {
		// When requesting not existing folder.. it should return empty list
		List<FileEntry> findAll = repo.findAll(getTestUser(), "/helloworld", null);
		assertThat(findAll.size(), is(0));
	}

	@Test
	public void testRecursiveDirSave() throws IOException {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent("HELLO WORLD2");
		fileEntry.setEncoding("UTF-8");
		fileEntry.setPath("/myworld/www/hello.zip");
		fileEntry.setEncoding(null);
		fileEntry.setFileType(FileType.UNKNOWN);
		byte[] byteArray = IOUtils.toByteArray(new ClassPathResource("TEST_USER.zip").getInputStream());
		fileEntry.setContentBytes(byteArray);
		repo.save(getTestUser(), fileEntry, null);
	}
	
	@Test
	public void test() throws Exception {
		// given
		String path = "/commit/test/";
		String file = "hello.txt";
		String filePath = path + file;
		String firstTxt = "hello";
		String secondTxt = "bye";
		File tmpDir = repo.getUserRepoDirectory(getTestUser());
		File testFile = new File(tmpDir, file);
		
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath(filePath);
		fileEntry.setFileType(FileType.TXT);
		
		// when
		fileEntry.setContent(firstTxt);
		repo.save(getTestUser(), fileEntry, null);
		FileEntry first = repo.findOne(getTestUser(), filePath, SVNRevision.HEAD);
		fileEntry.setContent(secondTxt);
		repo.save(getTestUser(), fileEntry, null);

		// then
		repo.writeContentTo(getTestUser(), filePath, tmpDir);
		assertFileContent(testFile, secondTxt);
		
		repo.writeContentTo(getTestUser(), filePath, tmpDir, first.getRevision());
		assertFileContent(testFile, firstTxt);
	}
	
	private void assertFileContent(File file, String content) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String readLine = br.readLine();
			assertTrue(readLine.equals(content));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(br);
		}
	}
}
