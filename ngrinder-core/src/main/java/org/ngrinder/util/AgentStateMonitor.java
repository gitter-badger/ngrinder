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

import java.io.Serializable;

/**
 * Record max of Cpu/Mem/Time value. 
 * 
 * @author Gisoo Gwon
 */
public interface AgentStateMonitor extends Serializable {

	/**
	 * The max recored cpu use per after last call clear()
	 * 
	 * @return
	 */
	public double getMaxCpuUsePer();

	/**
	 * The min recored free memory after last call clear()
	 * 
	 * @return
	 */
	public double getMinFreeMemory();
	
	/**
	 * The max recored time use milisec after last call clear()
	 * 
	 * @return
	 */
	public long getMaxUseTimeMilisec();
	
	/**
	 * The count of regist script.
	 * 
	 * @return
	 */
	public int getRegistScriptCount();
	
	/**
	 * The interval for repeat.
	 * 
	 * @return milisec
	 */
	public long getRepeatInterval();
	
	/**
	 * If new value is greater than max percent, then set new value.
	 * 
	 * @param percent new percent value
	 */
	public void recordCpuUsePer(double percent);
	
	/**
	 * If new value is less than min free memory, then set new value.
	 * 
	 * @param freeMemory new free memory value
	 */
	public void recordFreeMemory(double freeMemory);
	
	/**
	 * If new value is greater than max time, then set new value.
	 * 
	 * @param milisec time
	 */
	public void recordUseTime(long time);
	
	/**
	 * Set count of regist script.
	 * 
	 * @return
	 */
	public void setRegistScriptCount(int count);
	
	/**
	 * Set interval time for repeat.
	 * 
	 * @param repeatInterval milisec
	 */
	public void setRepeatInterval(long repeatInterval);
	
	/**
	 * Clear record info.
	 */
	public void clear();
	
}
