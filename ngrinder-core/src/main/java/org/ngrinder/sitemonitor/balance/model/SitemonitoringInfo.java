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
