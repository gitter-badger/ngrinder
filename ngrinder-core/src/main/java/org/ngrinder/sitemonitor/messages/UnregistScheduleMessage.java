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
package org.ngrinder.sitemonitor.messages;

import net.grinder.communication.Message;

/**
 * The unregist sitemonitoring message to Sitemonitor agent.
 * 
 * @author Gisoo Gwon
 */
public class UnregistScheduleMessage implements Message {
	private static final long serialVersionUID = -7272801198614843623L;
	
	private final String sitemonitorId;
	private final String scriptname;

	public UnregistScheduleMessage(String sitemonitorId, String scriptname) {
		this.sitemonitorId = sitemonitorId;
		this.scriptname = scriptname;
	}

	public String getSitemonitorId() {
		return sitemonitorId;
	}

	public String getScriptname() {
		return scriptname;
	}

}