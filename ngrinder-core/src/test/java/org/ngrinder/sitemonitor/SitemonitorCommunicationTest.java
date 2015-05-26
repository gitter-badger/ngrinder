package org.ngrinder.sitemonitor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.sitemonitor.MonitorScheduler;
import org.ngrinder.sitemonitor.SitemonitorSetting;
import org.ngrinder.sitemonitor.SitemonitorController;
import org.ngrinder.sitemonitor.SitemonitorControllerDaemon;
import org.ngrinder.sitemonitor.SitemonitorControllerServerDaemon;
import org.ngrinder.sitemonitor.SitemonitorControllerServerDaemon.FileDistributeListener;
import org.ngrinder.sitemonitor.messages.RegistScheduleMessage;
import org.ngrinder.sitemonitor.messages.UnregistScheduleMessage;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.Directory;
import net.grinder.util.ListenerSupport;
import net.grinder.util.thread.Condition;

/**
 * @author Gisoo Gwon
 */
public class SitemonitorCommunicationTest {
	private int freePort;

	@Before
	public void before() throws Exception {
		// find free port
		ServerSocket socket = new ServerSocket(0);
		freePort = socket.getLocalPort();
		socket.close();
	}

	@Test
	public void testAttatchToServerAndCommunication() throws Exception {
		// given
		int controllerCount = 2;
		MyAgentConfig myAgentConfig = new MyAgentConfig();
		myAgentConfig.init();
		MonitorScheduler scheduler = mock(MonitorScheduler.class);

		// run server daemon
		SitemonitorControllerServerDaemon serverDaemon = new SitemonitorControllerServerDaemon(
			freePort);
		serverDaemon.start();

		// run sitemonitor agent
		for (int i = 0; i < controllerCount; i++) {
			SitemonitorController controller = new SitemonitorController(myAgentConfig,
				new Condition());
			SitemonitorControllerDaemon controllerDaemon = new SitemonitorControllerDaemon(
				controller);
			controller.setMonitorScheduler(scheduler);
			controllerDaemon.run();
		}

		// then
		Thread.sleep(2000);
		assertThat(serverDaemon.getAllAvailableAgents().size(), is(controllerCount));

		sendRegistScheduleMessage(serverDaemon);
		Thread.sleep(100);
		sendUnregistScheduleMessage(serverDaemon);

		Thread.sleep(500);
		verify(scheduler, times(controllerCount)).regist(Matchers.any(SitemonitorSetting.class));
		verify(scheduler, times(controllerCount)).unregist(Matchers.any(SitemonitorSetting.class));

		Thread.sleep(2000);
		assertThat(serverDaemon.getAllAvailableAgents().size(), is(controllerCount));
	}

	@Test
	public void testFileDistribute() throws Exception {
		// init
		MyAgentConfig myAgentConfig = new MyAgentConfig();
		myAgentConfig.init();
		// run server daemon
		SitemonitorControllerServerDaemon serverDaemon = new SitemonitorControllerServerDaemon(
			freePort);
		serverDaemon.start();
		// run sitemonitor agent
		SitemonitorController controller = new SitemonitorController(myAgentConfig, new Condition());
		SitemonitorControllerDaemon controllerDaemon = new SitemonitorControllerDaemon(controller);
		controllerDaemon.run();

		// given
		Thread.sleep(2000);
		assertThat(serverDaemon.getAllAvailableAgents().size(), is(1));
		Set<AgentIdentity> allAvailableAgents = serverDaemon.getAllAvailableAgents();
		AgentIdentity agentIdentity = allAvailableAgents.toArray(new AgentIdentity[0])[0];
		ListenerSupport<FileDistributeListener> listenerSupport = new ListenerSupport<SitemonitorControllerServerDaemon.FileDistributeListener>();
		FileDistributeListener listener = mock(FileDistributeListener.class);
		listenerSupport.add(listener);
		File sendFolder = new File(getClass().getResource("/sendfolder").getFile());

		String groupName = "new";
		File downloadFolder = new File(myAgentConfig.getHome().getDirectory(),
			SitemonitorController.SITEMONITOR_FILE + File.separator + "incoming" + File.separator + groupName);
		FileUtils.deleteQuietly(downloadFolder);

		// when, send file
		serverDaemon.sendFile(new AgentAddress(agentIdentity), groupName, new Directory(sendFolder),
			Pattern.compile(ConsoleProperties.DEFAULT_DISTRIBUTION_FILE_FILTER_EXPRESSION),
			listenerSupport);
		Thread.sleep(1000);

		// then
		List<String> downloadedFiles = Arrays.asList(downloadFolder.list());

		for (File file : sendFolder.listFiles()) {
			if (file.isFile()) {
				verify(listener, times(1)).distributed(file.getName());
				assertThat(downloadedFiles, hasItems(file.getName()));
			}
		}
	}

	private void sendRegistScheduleMessage(SitemonitorControllerServerDaemon serverDaemon) {
		RegistScheduleMessage regist = new RegistScheduleMessage();
		regist.setSitemonitorSetting(new SitemonitorSetting(null, null, -1));
		serverDaemon.sendToAgents(regist);
	}

	private void sendUnregistScheduleMessage(SitemonitorControllerServerDaemon serverDaemon) {
		UnregistScheduleMessage regist = new UnregistScheduleMessage();
		regist.setSitemonitorSetting(new SitemonitorSetting(null, null, -1));
		serverDaemon.sendToAgents(regist);
	}

	class MyAgentConfig extends AgentConfig {
		@Override
		public String getSitemonitorControllerIp() {
			return "localhost";
		}

		@Override
		public int getSitemonitorControllerPort() {
			return freePort;
		}

		@Override
		public String getSitemonitorOwner() {
			return "";
		}
	}
}
