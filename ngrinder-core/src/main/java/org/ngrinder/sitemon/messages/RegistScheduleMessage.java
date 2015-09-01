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
package org.ngrinder.sitemon.messages;

import net.grinder.communication.Message;

/**
 * The regist sitemon message.
 * 
 * @author Gisoo Gwon
 */
public class RegistScheduleMessage implements Message {

	private static final long serialVersionUID = 8032597041213935838L;

	private final String siteMonId;
	private final String scriptname;
	private final String propHosts;
	private final String propParam;
	private final String errorCallback;
	
	/**
	 * The Constructor.
	 * 
	 * @param siteMonId
	 * @param scriptname
	 * @param propHosts
	 * @param propParam
	 */
	public RegistScheduleMessage(String siteMonId, String scriptname, String propHosts,
		String propParam, String errorCallback) {
		this.siteMonId = siteMonId;
		this.scriptname = scriptname;
		this.propHosts = propHosts;
		this.propParam = propParam;
		this.errorCallback = errorCallback;
	}

	public String getSiteMonId() {
		return siteMonId;
	}

	public String getScriptname() {
		return scriptname;
	}

	public String getPropHosts() {
		return propHosts;
	}

	public String getPropParam() {
		return propParam;
	}

	public String getErrorCallback() {
		return errorCallback;
	}

}
