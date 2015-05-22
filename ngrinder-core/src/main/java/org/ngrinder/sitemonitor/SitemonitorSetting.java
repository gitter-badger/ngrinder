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
package org.ngrinder.sitemonitor;

import java.io.Serializable;

/**
 * @author Gisoo Gwon
 */
public class SitemonitorSetting implements Serializable {
	private static final int TEN_SECOND = 10 * 1000;

	private static final long serialVersionUID = -1828068594209763814L;

	private String groupName = null;
	private String errorCallback = null;
	private int repeatCycle = TEN_SECOND;

	public SitemonitorSetting(String groupName, String errorCallback, int repeatCycle) {
		this.groupName = groupName;
		this.errorCallback = errorCallback;
		this.repeatCycle = repeatCycle;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getErrorCallback() {
		return errorCallback;
	}

	public void setErrorCallback(String errorCallback) {
		this.errorCallback = errorCallback;
	}

	public int getRepeatCycle() {
		return repeatCycle;
	}

	public void setRepeatCycle(int repeatCycle) {
		this.repeatCycle = repeatCycle;
	}
}
