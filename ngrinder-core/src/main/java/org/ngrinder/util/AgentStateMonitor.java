package org.ngrinder.util;

/**
 * Record max of Cpu/Mem/Time value. 
 * 
 * @author Gisoo Gwon
 */
public interface AgentStateMonitor {

	/**
	 * The max recored cpu use per after last call clear()
	 * 
	 * @return
	 */
	public double maxCpuUsePer();

	/**
	 * The min recored free memory after last call clear()
	 * 
	 * @return
	 */
	public double minFreeMemory();
	
	/**
	 * The max recored time use milisec after last call clear()
	 * 
	 * @return
	 */
	public long maxUseTimeMilisec();
	
	/**
	 * The count of regist script.
	 * 
	 * @return
	 */
	public int getRegistScriptCount();
	
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
	 * Clear record info.
	 */
	public void clear();
	
}
