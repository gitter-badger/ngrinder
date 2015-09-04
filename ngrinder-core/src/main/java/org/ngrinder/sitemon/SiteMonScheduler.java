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
package org.ngrinder.sitemon;

import java.util.List;

import org.ngrinder.sitemon.messages.RegistScheduleMessage;
import org.ngrinder.sitemon.model.SiteMonResult;
import org.ngrinder.sitemon.model.SiteMonResultLog;

/**
 * The sitemon run manager.
 * 
 * @author Gisoo Gwon
 */
public interface SiteMonScheduler {

	public void regist(RegistScheduleMessage message);

	public void unregist(String siteMonId);
	
	public List<SiteMonResult> pollAllSiteMonResult();
	
	public List<SiteMonResultLog> pollAllSiteMonResultLog();

	public void shutdown();
}
