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
package org.ngrinder.sitemonitor.balance.model;

import java.util.LinkedList;
import java.util.List;

import org.ngrinder.model.Sitemonitoring;

import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;

/**
 * @author Gisoo Gwon
 */
public class SitemonitoringInfo {
	
	private final List<Sitemonitoring> sitemonitorings = new LinkedList<Sitemonitoring>();
	private final AgentStatus agentStatus;

	public SitemonitoringInfo(AgentStatus agentStatus) {
		this.agentStatus = agentStatus;
	}
	
	public boolean addSitemonitoring(Sitemonitoring sitemonitoring) {
		return sitemonitorings.add(sitemonitoring);		
	}
	
	public List<Sitemonitoring> extractRebalanceTarget() {
		if (agentStatus == null) {	// agent shutdown
			return sitemonitorings;
		}
		List<Sitemonitoring> targets = new LinkedList<Sitemonitoring>();
		int registableCount = agentStatus.guessMoreRunnableScriptCount();
		int overRegistCount = -registableCount;
		for (int i = 0; i < overRegistCount; i++) {
			targets.add(sitemonitorings.remove(0));
		}
		return targets;
	}
	
}
