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
package org.ngrinder.util;

import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.collector.SystemDataCollector;
import org.ngrinder.monitor.share.domain.SystemInfo;

/**
 * Agent hardware resource monitor.
 * 
 * @author Gisoo Gwon
 */
public class AgentStateMonitorImplementation implements AgentStateMonitor {
	
	private static final long serialVersionUID = 321506172724636084L;
	
	private transient SystemDataCollector collector = new SystemDataCollector();
	private double maxCpuUsePer = 0;
	private double minFreeMemory = Double.MAX_VALUE;
	private long maxUseTimeMilisec  = 0;
	private int registScriptCount = 0;
	private long repeatInterval = 0;
	
	public AgentStateMonitorImplementation(AgentConfig config) {
		collector.setAgentHome(config.getHome().getDirectory());
		collector.refresh();
		SystemDataCollectorThread collectorThread = new SystemDataCollectorThread();
		collectorThread.setDaemon(true);
		collectorThread.start();
	}

	@Override
	public double getMaxCpuUsePer() {
		return maxCpuUsePer;
	}

	@Override
	public double getMinFreeMemory() {
		return minFreeMemory;
	}

	@Override
	public long getMaxUseTimeMilisec() {
		return maxUseTimeMilisec;
	}

	@Override
	public int getRegistScriptCount() {
		return registScriptCount;
	}

	@Override
	public long getRepeatInterval() {
		return repeatInterval;
	}

	@Override
	public void recordCpuUsePer(double percent) {
		if (percent > maxCpuUsePer) {
			maxCpuUsePer = percent;
		}
	}

	@Override
	public void recordFreeMemory(double freeMemory) {
		if (freeMemory < minFreeMemory) {
			minFreeMemory = freeMemory;
		}
	}

	@Override
	public void recordUseTime(long time) {
		if (time > maxUseTimeMilisec) {
			maxUseTimeMilisec = time;
		}
	}

	@Override
	public void setRegistScriptCount(int registScriptCount) {
		this.registScriptCount = registScriptCount;
	}

	@Override
	public void setRepeatInterval(long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	@Override
	public void clear() {
		maxCpuUsePer = 0;
		minFreeMemory = Double.MAX_VALUE;
		maxUseTimeMilisec = 0;
		registScriptCount = 0;
		collect();
	}
	
	private void collect() {
		SystemInfo systemInfo = collector.execute();
		recordCpuUsePer(systemInfo.getCPUUsedPercentage());
		recordFreeMemory(systemInfo.getFreeMemory());
	}
	
	class SystemDataCollectorThread extends Thread {
		
		private final long ONE_SEC = 1 * 1000;
		private long collectDuration = ONE_SEC;

		@Override
		public void run() {
			while (true) {
				collect();
				ThreadUtils.sleep(collectDuration);
			}
		}
		
	}

}
