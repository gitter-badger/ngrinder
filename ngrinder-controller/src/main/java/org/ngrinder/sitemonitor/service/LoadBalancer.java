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
package org.ngrinder.sitemonitor.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.DateUtils;
import org.ngrinder.model.Sitemonitoring;
import org.ngrinder.sitemonitor.balance.AbstractLoadBalancer;
import org.ngrinder.sitemonitor.repository.SitemonitoringRepository;
import org.ngrinder.util.AgentStateMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;

/**
 * Execute repeat rebalance sitemonitoring.
 * @author Gisoo Gwon
 */
@Component
public class LoadBalancer extends AbstractLoadBalancer {

	private static final long PERIOD = 1 * DateUtils.MILLIS_PER_MINUTE;	// 1 min
	private static final long DELAY = 5 * DateUtils.MILLIS_PER_MINUTE;	// 5 min

	@Autowired
	private SitemonitorManagerService sitemonitorManagerService;

	@Autowired
	private SitemonitoringRepository sitemonitoringRepository;

	public LoadBalancer() {
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Set<AgentStatus> statusList = sitemonitorManagerService.getAllAgentStatus();
				List<Sitemonitoring> monitorList = sitemonitoringRepository.findAll();
				executeRebalance(statusList, monitorList);
			}
		}, DELAY, PERIOD);
	}

	/**
	 * 
	 */
	@Override
	protected boolean isExecuteRebalanceState(Collection<AgentStatus> agentStatusList,
		Collection<Sitemonitoring> sitemonitorings) {
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
	protected void unregistSitemonitoring(Sitemonitoring sitemonitoring) {
		// sitemonitorManagerService.addSitemonitoring() is contain unregist logic.
	}

	@Override
	protected void registSitemonitoring(Sitemonitoring sitemonitoring) {
		try {
			sitemonitorManagerService.addSitemonitoring(sitemonitoring);
		} catch (Exception e) {
			LOGGER.error("Fail rebalance sitemonitoring {} : {}", sitemonitoring.getId(),
				e.getMessage());
			e.printStackTrace();
		}
	}

}
