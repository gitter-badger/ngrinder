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
package org.ngrinder.sitemon.balance.model;

import java.util.LinkedList;
import java.util.List;

import org.ngrinder.model.SiteMon;

import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;

/**
 * The relation meta info of sitemon and agent.
 * 
 * @author Gisoo Gwon
 */
public class SiteMonInfo {
	
	private final AgentStatus agentStatus;
	private final List<SiteMon> siteMons = new LinkedList<SiteMon>();

	public SiteMonInfo(AgentStatus agentStatus) {
		this.agentStatus = agentStatus;
	}
	
	public boolean addSiteMon(SiteMon siteMon) {
		return siteMons.add(siteMon);		
	}
	
	public List<SiteMon> extractRebalanceTarget() {
		if (agentStatus == null) {	// agent shutdown
			return siteMons;
		}
		List<SiteMon> targets = new LinkedList<SiteMon>();
		int registableCount = agentStatus.guessMoreRunnableScriptCount();
		int overRegistCount = -registableCount;
		for (int i = 0; i < overRegistCount; i++) {
			targets.add(siteMons.remove(0));
		}
		return targets;
	}
	
}
