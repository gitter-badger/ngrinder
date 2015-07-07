package org.ngrinder.sitemonitor.balance;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ngrinder.model.Sitemonitoring;
import org.ngrinder.sitemonitor.balance.model.SitemonitoringInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;

/**
 * @author Gisoo Gwon
 */
public abstract class AbstractLoadBalancer {

	public static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoadBalancer.class);

	/**
	 * 
	 * @param statusList conntected agent status list.
	 * @param monitorList registed sitemonitoring list.
	 */
	public void executeRebalance(Collection<AgentStatus> statusList,
		Collection<Sitemonitoring> monitorList) {
		if (!isExecuteRebalanceState(statusList, monitorList)) {
			return;
		}
		Map<String, SitemonitoringInfo> infoMap = createSitemonitoringInfos(statusList, monitorList);
		List<Sitemonitoring> rebalanceTarget = extractRebalanceTarget(infoMap);
		List<String> idleAgentNames = getIdleAgentList(statusList, rebalanceTarget);
		LOGGER.info("Re balance target size is {}. Total registable count is {}.",
			rebalanceTarget.size(), idleAgentNames.size());

		for (String idleAgentName : idleAgentNames) {
			if (rebalanceTarget.size() == 0) {
				break;
			}
			Sitemonitoring target = rebalanceTarget.remove(0);
			unregistSitemonitoring(target);
			target.setAgentName(idleAgentName);
			registSitemonitoring(target);
		}
	}

	protected abstract void unregistSitemonitoring(Sitemonitoring sitemonitoring);

	protected abstract void registSitemonitoring(Sitemonitoring sitemonitoring);

	private List<String> getIdleAgentList(Collection<AgentStatus> statusList,
		Collection<Sitemonitoring> rebalanceTarget) {
		List<String> idleAgentNames = new LinkedList<String>();
		for (AgentStatus agentStatus : statusList) {
			int registableCnt = agentStatus.guessMoreRunnableScriptCount();
			for (int i = 0; i < registableCnt; i++) {
				idleAgentNames.add(agentStatus.getAgentName());
			}
		}
		return idleAgentNames;
	}

	/**
	 * @param infoMap Map<AgentName, SitemonitoringInfo>
	 * @return rebalance target list.
	 */
	private List<Sitemonitoring> extractRebalanceTarget(Map<String, SitemonitoringInfo> infoMap) {
		List<Sitemonitoring> rebalanceTarget = new LinkedList<Sitemonitoring>();
		for (String agentName : infoMap.keySet()) {
			SitemonitoringInfo info = infoMap.get(agentName);
			rebalanceTarget.addAll(info.extractRebalanceTarget());
		}
		return rebalanceTarget;
	}

	/**
	 * @param statusList
	 * @param monitorList
	 * @return Map<AgentName, SitemonitorInfo>
	 */
	private Map<String, SitemonitoringInfo> createSitemonitoringInfos(
		Collection<AgentStatus> statusList, Collection<Sitemonitoring> monitorList) {
		Map<String, SitemonitoringInfo> map = new HashMap<String, SitemonitoringInfo>();
		for (Sitemonitoring monitor : monitorList) {
			String agentName = monitor.getAgentName();
			if (map.get(agentName) == null) {
				AgentStatus agentStatus = findAgentStatus(statusList, agentName);
				SitemonitoringInfo info = new SitemonitoringInfo(agentStatus);
				map.put(agentName, info);
			}
			map.get(agentName).addSitemonitoring(monitor);
		}
		return map;
	}

	private AgentStatus findAgentStatus(Collection<AgentStatus> statusList, String agentName) {
		for (AgentStatus agentStatus : statusList) {
			if (agentName.equals(agentStatus.getAgentName())) {
				return agentStatus;
			}
		}
		return null;
	}

	/**
	 * Hook method for re balance execute control.
	 * @return Default true. if want control, override method. 
	 */
	protected boolean isExecuteRebalanceState(Collection<AgentStatus> agentStatusList,
		Collection<Sitemonitoring> sitemonitorings) {
		return true;
	}

}
