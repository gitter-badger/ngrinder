package org.ngrinder.sitemonitor;

import java.io.File;
import java.util.Set;

import org.ngrinder.sitemonitor.messages.AddScriptMessage;
import org.ngrinder.sitemonitor.messages.RemoveScriptMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.communication.CommunicationException;
import net.grinder.engine.agent.SitemonitorScriptRunner;
import net.grinder.engine.process.SitemonitorProcess;

/**
 * Managing process for execute sitemonitoring script.
 * 
 * @author Gisoo Gwon
 */
public class MonitorSchedulerImplementation implements MonitorScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger("monitor scheduler impl");

	private final SitemonitorScriptRunner scriptRunner;
	/**
	 * serverDaemon is receive message by {@link SitemonitorProcess}.
	 * Use serverDaemon.getComponent(ConsoleCommunication.class).getMessageDispatchRegistry()
	 */
	private final SitemonitorControllerServerDaemon serverDaemon;
	private final File baseDirectory;

	/**
	 * The constructor.
	 * 
	 * @param agentConfig
	 */
	public MonitorSchedulerImplementation(SitemonitorControllerServerDaemon serverDaemon,
		File baseDirectory) {
		scriptRunner = new SitemonitorScriptRunner(serverDaemon.getPort());
		this.serverDaemon = serverDaemon;
		this.baseDirectory = baseDirectory;
	}

	/**
	 * @param groupName
	 * @param scriptType
	 */
	@Override
	public void startProcess(SitemonitorSetting sitemonitorSetting) {
		scriptRunner.initWithThread(sitemonitorSetting, baseDirectory);
	}

	/**
	 * @param groupName
	 * @param scriptpath
	 */
	@Override
	public void regist(String groupName, String scriptpath) {
		try {
			scriptRunner.sendMessage(groupName, new AddScriptMessage(scriptpath));
		} catch (CommunicationException e) {
			LOGGER.error("Failed regist '{}' file : {}", scriptpath, e.getMessage());
		}
	}

	/**
	 * @param groupName
	 * @param scriptpath
	 */
	@Override
	public void unregist(String groupName, String scriptpath) {
		try {
			scriptRunner.sendMessage(groupName, new RemoveScriptMessage(scriptpath));
		} catch (CommunicationException e) {
			LOGGER.error("Failed unregist '{}' file : {}", scriptpath, e.getMessage());
		}
	}

	/**
	 * destroy process.
	 */
	@Override
	public void shutdown() {
		scriptRunner.shutdown();
	}
	
	public Set<String> getRunningGroups() {
		return scriptRunner.getRunningGroups();
	}

}
