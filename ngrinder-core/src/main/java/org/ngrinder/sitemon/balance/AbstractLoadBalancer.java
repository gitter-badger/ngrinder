package org.ngrinder.sitemon.balance;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ngrinder.model.SiteMon;
import org.ngrinder.sitemon.balance.model.SiteMonInfo;
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
	 * @param siteMonList registed sitemon list.
	 */
	public void executeRebalance(Collection<AgentStatus> statusList,
		Collection<SiteMon> siteMonList) {
		if (!isExecuteRebalanceState(statusList, siteMonList)) {
			return;
		}
		Map<String, SiteMonInfo> infoMap = createSiteMonInfoList(statusList, siteMonList);
		List<SiteMon> rebalanceTarget = extractRebalanceTarget(infoMap);
		List<String> idleAgentNames = getIdleAgentList(statusList, rebalanceTarget);
		LOGGER.info("Re balance target size is {}. Total registable count is {}.",
			rebalanceTarget.size(), idleAgentNames.size());

		for (String idleAgentName : idleAgentNames) {
			if (rebalanceTarget.size() == 0) {
				break;
			}
			SiteMon target = rebalanceTarget.remove(0);
			target.setAgentName(idleAgentName);
			rebalanceSiteMon(target);
		}
	}

	protected abstract void rebalanceSiteMon(SiteMon siteMon);

	private List<String> getIdleAgentList(Collection<AgentStatus> statusList,
		Collection<SiteMon> rebalanceTarget) {
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
	 * @param infoMap Map<AgentName, SiteMonInfo>
	 * @return rebalance target list.
	 */
	private List<SiteMon> extractRebalanceTarget(Map<String, SiteMonInfo> infoMap) {
		List<SiteMon> rebalanceTarget = new LinkedList<SiteMon>();
		for (String agentName : infoMap.keySet()) {
			SiteMonInfo info = infoMap.get(agentName);
			rebalanceTarget.addAll(info.extractRebalanceTarget());
		}
		return rebalanceTarget;
	}

	/**
	 * @param statusList
	 * @param monitorList
	 * @return Map<AgentName, SIteMonInfo>
	 */
	private Map<String, SiteMonInfo> createSiteMonInfoList(
		Collection<AgentStatus> statusList, Collection<SiteMon> monitorList) {
		Map<String, SiteMonInfo> map = new HashMap<String, SiteMonInfo>();
		for (SiteMon monitor : monitorList) {
			String agentName = monitor.getAgentName();
			if (map.get(agentName) == null) {
				AgentStatus agentStatus = findAgentStatus(statusList, agentName);
				SiteMonInfo info = new SiteMonInfo(agentStatus);
				map.put(agentName, info);
			}
			map.get(agentName).addSiteMon(monitor);
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
		Collection<SiteMon> siteMonList) {
		return true;
	}

}
