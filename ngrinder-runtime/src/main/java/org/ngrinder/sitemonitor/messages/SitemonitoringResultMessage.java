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

import java.util.LinkedList;
import java.util.List;

import org.ngrinder.sitemonitor.model.SitemonitoringResult;

import net.grinder.communication.Message;

/**
 * Message of sitemonitoring execute result.
 * 
 * @author Gisoo Gwon
 */
public class SitemonitoringResultMessage implements Message {

	private static final long serialVersionUID = -2658696439516448216L;
	
	private final List<SitemonitoringResult> results;

	public SitemonitoringResultMessage() {
		this.results = new LinkedList<SitemonitoringResult>();
	}

	public SitemonitoringResultMessage(List<SitemonitoringResult> results) {
		this.results = new LinkedList<SitemonitoringResult>(results);
	}
	
	public void addResults(List<SitemonitoringResult> results) {
		this.results.addAll(results);
	}

	public List<SitemonitoringResult> getSitemonitoringResults() {
		return results;
	}

}
