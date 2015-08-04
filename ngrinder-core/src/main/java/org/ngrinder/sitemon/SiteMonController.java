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
package org.ngrinder.sitemon;

import static org.ngrinder.common.constants.InternalConstants.*;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.DateUtils;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.sitemon.messages.RegistScheduleMessage;
import org.ngrinder.sitemon.messages.ShutdownServerMessage;
import org.ngrinder.sitemon.messages.SiteMonReloadMessage;
import org.ngrinder.sitemon.messages.SiteMonResultMessage;
import org.ngrinder.sitemon.messages.UnregistScheduleMessage;
import org.ngrinder.sitemon.model.SiteMonResult;
import org.ngrinder.util.AgentStateMonitor;
import org.python.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.GrinderConstants;
import net.grinder.common.GrinderException;
import net.grinder.communication.ClientReceiver;
import net.grinder.communication.ClientSender;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.ConnectionType;
import net.grinder.communication.Connector;
import net.grinder.communication.Message;
import net.grinder.communication.MessageDispatchSender;
import net.grinder.communication.MessagePump;
import net.grinder.engine.agent.Agent;
import net.grinder.engine.agent.FileStoreSupport;
import net.grinder.engine.common.AgentControllerConnectorFactory;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerProcessReportMessage;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.NetworkUtils;
import net.grinder.util.thread.Condition;

/**
 * @author Gisoo Gwon
 */
public class SiteMonController implements Agent {

	private static final Logger LOGGER = LoggerFactory.getLogger("sitemon controller");

	private final File scriptBaseDirectory;

	private final AgentControllerIdentityImplementation agentIdentity;
	private final Condition eventSyncCondition;
	private final AgentConfig agentConfig;
	private boolean connected = false;
	private boolean shutdownServer = false;
	private String version;
	private MonitorScheduler monitorScheduler;
	private AgentStateMonitor agentStateMonitor;
	private SiteMonControllerServerListener siteMonControllerServerListener;
	private final AgentControllerConnectorFactory connectorFactory = new AgentControllerConnectorFactory(
		ConnectionType.AGENT);
	private ClientSender clientSender;
	private MessagePump messagePump;
	private Timer sendStatusTimer;
	private MessageDispatchSender messageDispatcher;

	public SiteMonController(File scriptBaseDirectory, AgentConfig agentConfig,
		Condition eventSyncCondition) {
		this.scriptBaseDirectory = scriptBaseDirectory;
		this.agentConfig = agentConfig;
		this.eventSyncCondition = eventSyncCondition;
		this.version = agentConfig.getInternalProperties().getProperty(
			PROP_INTERNAL_NGRINDER_VERSION);

		// TODO : agent_sitemon.conf should be support setting that agent host id
		agentIdentity = new AgentControllerIdentityImplementation(agentConfig.getAgentHostID(),
			NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS);
		siteMonControllerServerListener = new SiteMonControllerServerListener(
			eventSyncCondition, LOGGER);
	}

	@Override
	public void run() throws GrinderException {
		synchronized (eventSyncCondition) {
			eventSyncCondition.notifyAll();
		}

		try {
			setShutdownServer(false);
			do {
				if (!connected) {
					Connector connector = null;
					try {
						String ip = agentConfig.getSiteMonAgentControllerIp();
						int port = agentConfig.getSiteMonAgentControllerPort();
						connector = connectorFactory.create(ip, port);

						connect(connector);
						initFileStore();
						LOGGER.info("Connected to sitemon controller server at {}",
							connector.getEndpointAsString());
					} catch (CommunicationException e) {
						LOGGER.error(
							"Error while connecting to sitemon controller server at {}",
							connector.getEndpointAsString());
						return;
					}
					continue;
				}

				Message message = siteMonControllerServerListener.waitForMessage();
				handle(message);
			} while (!isShutdownServer());
		} finally {
			shutdown();
		}
	}

	@Override
	public void shutdown() {
		if (sendStatusTimer != null) {
			sendStatusTimer.cancel();
		}
		if (clientSender != null) {
			clientSender.shutdown();
			clientSender = null;
		}
		if (messagePump != null) {
			messagePump.shutdown();
			messagePump = null;
		}
		if (siteMonControllerServerListener != null) {
			siteMonControllerServerListener.shutdown();
		}
		connected = false;
	}

	private void handle(Message message) throws EngineException {
		if (message instanceof ShutdownServerMessage) {
			setShutdownServer(true);
		} else if (message instanceof RegistScheduleMessage) {
			RegistScheduleMessage registSchedule = (RegistScheduleMessage) message;
			monitorScheduler.regist(registSchedule);
		} else if (message instanceof UnregistScheduleMessage) {
			UnregistScheduleMessage unregistSchedule = (UnregistScheduleMessage) message;
			monitorScheduler.unregist(unregistSchedule.getSiteMonId());
		} else {
			LOGGER.warn("received invalid message");
		}
	}

	public void setMonitorScheduler(MonitorScheduler monitorScheduler) {
		this.monitorScheduler = monitorScheduler;
	}

	public void setAgentStateMonitor(AgentStateMonitor agentStateMonitor) {
		this.agentStateMonitor = agentStateMonitor;
	}

	private void sendCurrentState() throws CommunicationException {
		AgentControllerProcessReportMessage message = new AgentControllerProcessReportMessage(
			AgentControllerState.STARTED, new SystemDataModel(),
			agentConfig.getSiteMonAgentControllerPort(), version);
		message.setAgentStateMonitor(agentStateMonitor);
		clientSender.send(message);
	}

	private synchronized boolean isShutdownServer() {
		return shutdownServer;
	}

	private synchronized void setShutdownServer(boolean shutdownServer) {
		this.shutdownServer = shutdownServer;
	}

	private void initFileStore() throws EngineException {
		Preconditions.checkNotNull(messageDispatcher);
		FileStoreSupport fileStoreSupport = new FileStoreSupport(scriptBaseDirectory,
			messageDispatcher, LOGGER);
		fileStoreSupport.ignoreClearCacheMessage();
	}

	private synchronized void connect(Connector connector) throws CommunicationException,
		EngineException {
		ClientReceiver receiver = ClientReceiver.connect(connector, new AgentAddress(agentIdentity));

		messageDispatcher = new MessageDispatchSender();
		siteMonControllerServerListener.registerMessageHandlers(messageDispatcher);
		messagePump = new MessagePump(receiver, messageDispatcher, 1);
		messagePump.start();

		clientSender = ClientSender.connect(receiver);

		sendStatusTimer = new Timer(false);
		sendStatusTimer.schedule(new TimerTask() {
			public void run() {
				try {
					sendCurrentState();
				} catch (CommunicationException e) {
					cancel();
					LOGGER.error("Error while sending current state:" + e.getMessage());
					LOGGER.debug("The error detail is", e);
				}
			}
		}, 0, GrinderConstants.AGENT_CONTROLLER_HEARTBEAT_INTERVAL);
		sendStatusTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					clientSender.send(new SiteMonReloadMessage());
					LOGGER.debug("Send reload sitemon setting to server.");
				} catch (CommunicationException e) {
					LOGGER.error("Fail send reload sitemon setting to server.");
				}
			}
		}, 3000);
		sendStatusTimer.schedule(new TimerTask() {
			public void run() {
				List<SiteMonResult> results = monitorScheduler.pollAllResults();
				if (results.size() > 0) {
					SiteMonResultMessage message = new SiteMonResultMessage(results);
					try {
						clientSender.send(message);
					} catch (Exception e) {
						LOGGER.error("Failed send sitemon result message. {}",
							e.getMessage());
					}
				}
			}
		}, 0, agentStateMonitor == null ? DateUtils.MILLIS_PER_MINUTE : agentStateMonitor.getRepeatInterval());
		connected = true;
	}
}
