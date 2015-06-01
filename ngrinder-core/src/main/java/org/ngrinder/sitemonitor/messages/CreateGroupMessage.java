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

import org.ngrinder.sitemonitor.SitemonitorSetting;

import net.grinder.communication.Message;

/**
 * @author Gisoo Gwon
 */
public class CreateGroupMessage implements Message {
	
	private static final long serialVersionUID = -4558006969376493960L;
	
	private SitemonitorSetting sitemonitorSetting;
	
	public CreateGroupMessage(SitemonitorSetting sitemonitorSetting) {
		this.sitemonitorSetting = sitemonitorSetting;
	}

	public SitemonitorSetting getSitemonitorSetting() {
		return sitemonitorSetting;
	}

	public void setSitemonitorSetting(SitemonitorSetting sitemonitorSetting) {
		this.sitemonitorSetting = sitemonitorSetting;
	}
	
}
