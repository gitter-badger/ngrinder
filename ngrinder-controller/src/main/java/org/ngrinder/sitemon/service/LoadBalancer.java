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
package org.ngrinder.sitemon.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.DateUtils;
import org.ngrinder.model.SiteMon;
import org.ngrinder.sitemon.balance.AbstractLoadBalancer;
import org.ngrinder.sitemon.repository.SiteMonRepository;
import org.ngrinder.util.AgentStateMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;

/**
 * Execute repeat rebalance sitemon.
 * @author Gisoo Gwon
 */
@Component
public class LoadBalancer extends AbstractLoadBalancer {

	private static final long PERIOD = 1 * DateUtils.MILLIS_PER_MINUTE;	// 1 min
	private static final long DELAY = 5 * DateUtils.MILLIS_PER_MINUTE;	// 5 min

	@Autowired
	private SiteMonAgentManagerService siteMonAgentManagerService;

	@Autowired
	private SiteMonRepository siteMonRepository;

	public LoadBalancer() {
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Set<AgentStatus> statusList = siteMonAgentManagerService.getAllAgentStatus();
				List<SiteMon> siteMonList = siteMonRepository.findByRunState(true);
				executeRebalance(statusList, siteMonList);
			}
		}, DELAY, PERIOD);
	}

	/**
	 * 
	 */
	@Override
	protected boolean isExecuteRebalanceState(Collection<AgentStatus> agentStatusList,
		Collection<SiteMon> siteMonList) {
		for (AgentStatus agentStatus : agentStatusList) {
			AgentStateMonitor agentStateMonitor = agentStatus.getAgentStateMonitor();
			if (agentStateMonitor.getMaxUseTimeMilisec() == 0
				&& agentStateMonitor.getRegistScriptCount() > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void rebalanceSiteMon(SiteMon siteMon, String newAgentName) {
		try {
			siteMonAgentManagerService.sendUnregist(siteMon.getId());
			siteMonAgentManagerService.sendRegist(siteMon, newAgentName);
			siteMon.setAgentName(newAgentName);
			siteMonRepository.saveAndFlush(siteMon);
		} catch (Exception e) {
			LOGGER.error("Fail rebalance sitemon {} : {}", siteMon.getId(), e.getMessage());
			e.printStackTrace();
		}
	}

}
