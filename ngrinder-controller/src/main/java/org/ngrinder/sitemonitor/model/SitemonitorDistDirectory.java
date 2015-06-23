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
package org.ngrinder.sitemonitor.model;

import java.io.File;

/**
 * @author Gisoo Gwon
 */
public class SitemonitorDistDirectory {
	private File RootDirectory;
	private File ScriptDirectory;

	public File getRootDirectory() {
		return RootDirectory;
	}

	public void setRootDirectory(File rootDirectory) {
		RootDirectory = rootDirectory;
	}

	public File getScriptDirectory() {
		return ScriptDirectory;
	}

	public void setScriptDirectory(File scriptDirectory) {
		ScriptDirectory = scriptDirectory;
	}

}
