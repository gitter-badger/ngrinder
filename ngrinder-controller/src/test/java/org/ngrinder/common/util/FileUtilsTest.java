package org.ngrinder.common.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class FileUtilsTest {
	private static final int FILE_COUNT = 3;

	@Test
	public void testListContainSubFolder() throws Exception {
		File root = new File("root");
		File subFolder2 = new File(root, "subFolder2");
		File subFolder = new File(root, "subFolder");
		File file = new File(root, "file");
		File subFile = new File(subFolder, "file");
		File subFile2 = new File(subFolder2, "file");

		root.mkdir();
		subFolder.mkdir();
		subFolder2.mkdir();
		file.createNewFile();
		subFile.createNewFile();
		subFile2.createNewFile();

		List<File> all = FileUtils.listContainSubFolder(root, new LinkedList<File>());

		assertThat(all.size(), is(FILE_COUNT));
		assertThat(all, hasItem(file));
		assertThat(all, hasItem(subFile));
		assertThat(all, hasItem(subFile2));

		org.apache.commons.io.FileUtils.deleteQuietly(root);
	}
}
