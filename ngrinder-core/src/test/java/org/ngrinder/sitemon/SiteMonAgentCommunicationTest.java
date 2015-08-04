package org.ngrinder.sitemon;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.sitemon.MonitorScheduler;
import org.ngrinder.sitemon.SiteMonController;
import org.ngrinder.sitemon.SiteMonControllerDaemon;
import org.ngrinder.sitemon.SiteMonControllerServerDaemon;
import org.ngrinder.sitemon.SiteMonControllerServerDaemon.FileDistributeListener;
import org.ngrinder.sitemon.messages.RegistScheduleMessage;
import org.ngrinder.sitemon.messages.UnregistScheduleMessage;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.Directory;
import net.grinder.util.ListenerSupport;
import net.grinder.util.NetworkUtils;
import net.grinder.util.thread.Condition;

/**
 * @author Gisoo Gwon
 */
public class SiteMonAgentCommunicationTest {
	private int freePort;
	private String siteMonId = "monitor1";
	private String scriptname = "scriptname";
	private String hosts = "www.naver.com:123.123.123.123,localhost:127.0.0.1";
	private String param = "param|value.1";
	private File baseDirectory = new File(getClass().getResource("/").getFile());

	@Before
	public void before() throws Exception {
		freePort = NetworkUtils.getFreePortOfLocal();
	}

	@Test
	public void testAttatchToServerAndCommunication() throws Exception {
		// given
		int controllerCount = 2;
		MyAgentConfig myAgentConfig = new MyAgentConfig();
		myAgentConfig.init();
		MonitorScheduler scheduler = mock(MonitorScheduler.class);

		// run server daemon
		SiteMonControllerServerDaemon serverDaemon = new SiteMonControllerServerDaemon(
			freePort);
		serverDaemon.start();

		// run sitemon agent
		for (int i = 0; i < controllerCount; i++) {
			SiteMonController controller = new SiteMonController(baseDirectory,
				myAgentConfig, new Condition());
			SiteMonControllerDaemon controllerDaemon = new SiteMonControllerDaemon(
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
		verify(scheduler, times(controllerCount)).regist((RegistScheduleMessage) any());
		verify(scheduler, times(controllerCount)).unregist(siteMonId);

		Thread.sleep(2000);
		assertThat(serverDaemon.getAllAvailableAgents().size(), is(controllerCount));
	}

	@Test
	public void testFileDistribute() throws Exception {
		// init
		MyAgentConfig myAgentConfig = new MyAgentConfig();
		myAgentConfig.init();
		// run server daemon
		SiteMonControllerServerDaemon serverDaemon = new SiteMonControllerServerDaemon(
			freePort);
		serverDaemon.start();
		// run sitemon agent
		SiteMonController controller = new SiteMonController(baseDirectory, myAgentConfig,
			new Condition());
		SiteMonControllerDaemon controllerDaemon = new SiteMonControllerDaemon(controller);
		controllerDaemon.run();

		// given
		Thread.sleep(2000);
		assertThat(serverDaemon.getAllAvailableAgents().size(), is(1));
		Set<AgentIdentity> allAvailableAgents = serverDaemon.getAllAvailableAgents();
		AgentIdentity agentIdentity = allAvailableAgents.toArray(new AgentIdentity[0])[0];
		ListenerSupport<FileDistributeListener> listenerSupport = new ListenerSupport<SiteMonControllerServerDaemon.FileDistributeListener>();
		FileDistributeListener listener = mock(FileDistributeListener.class);
		listenerSupport.add(listener);
		File sendFolder = new File(getClass().getResource("/sendfolder").getFile());

		File downloadFolder = new File(baseDirectory, File.separator + "incoming");
		FileUtils.deleteQuietly(downloadFolder);

		// when, send file
		serverDaemon.sendFile(new AgentAddress(agentIdentity), new Directory(sendFolder),
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

	private void sendRegistScheduleMessage(SiteMonControllerServerDaemon serverDaemon) {
		RegistScheduleMessage regist = new RegistScheduleMessage(siteMonId, scriptname, hosts, param);
		serverDaemon.sendToAgents(regist);
	}

	private void sendUnregistScheduleMessage(SiteMonControllerServerDaemon serverDaemon) {
		UnregistScheduleMessage unregist = new UnregistScheduleMessage(siteMonId);
		serverDaemon.sendToAgents(unregist);
	}

	class MyAgentConfig extends AgentConfig {
		@Override
		public String getSiteMonAgentControllerIp() {
			return "localhost";
		}

		@Override
		public int getSiteMonAgentControllerPort() {
			return freePort;
		}

		@Override
		public String getSiteMonAgentOwner() {
			return "";
		}
	}
}
