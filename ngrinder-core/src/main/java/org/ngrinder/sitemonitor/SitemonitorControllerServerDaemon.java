package org.ngrinder.sitemonitor;

import java.util.Set;
import java.util.Timer;
import java.util.regex.Pattern;

import net.grinder.AgentControllerServerDaemon;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.Address;
import net.grinder.communication.Message;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.console.communication.DistributionControlImplementationAddressDecorator;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.communication.DistributionControl;
import net.grinder.console.communication.ProcessControl;
import net.grinder.console.communication.ProcessControlImplementation;
import net.grinder.console.distribution.FileDistribution;
import net.grinder.console.distribution.FileDistributionHandler;
import net.grinder.console.distribution.FileDistributionHandler.Result;
import net.grinder.console.distribution.FileDistributionImplementation;
import net.grinder.util.Directory;
import net.grinder.util.FileContents.FileContentsException;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;

/**
 * Wrapping AgentControllerServerDaemon
 * @author Gisoo Gwon
 */
public class SitemonitorControllerServerDaemon {
	private AgentControllerServerDaemon serverDamon;
	private final Timer timer;

	public SitemonitorControllerServerDaemon(int port) {
		serverDamon = new AgentControllerServerDaemon(port);
		timer = new Timer();
		init();
	}

	public void start() {
		serverDamon.start();
	}

	public void sendToAgents(Message message) {
		ConsoleCommunication consoleCommunication = serverDamon.getComponent(ConsoleCommunication.class);
		consoleCommunication.sendToAgents(message);
	}

	public void sendToAddressedAgents(Address address, Message message) {
		ConsoleCommunication consoleCommunication = serverDamon.getComponent(ConsoleCommunication.class);
		consoleCommunication.sendToAddressedAgents(address, message);
	}

	public Set<AgentIdentity> getAllAvailableAgents() {
		return serverDamon.getComponent(AgentProcessControlImplementation.class).getAllAgents();
	}

	public void sendFile(Address address, Directory directory, Pattern fileFilterPattern,
		ListenerSupport<FileDistributeListener> listener) throws FileContentsException {

		ConsoleCommunication consoleCommunication = serverDamon.getComponent(ConsoleCommunication.class);
		DistributionControl distributionControl = new DistributionControlImplementationAddressDecorator(
			consoleCommunication, address);
		ProcessControl processControl = new ProcessControlImplementation(timer,
			consoleCommunication);
		FileDistribution fileDistribution = new FileDistributionImplementation(distributionControl,
			processControl, directory, fileFilterPattern);
		FileDistributionHandler handler = fileDistribution.getHandler();

		while (true) {
			final Result result = handler.sendNextFile();

			if (result == null) {
				break;
			}
			if (listener != null) {
				listener.apply(new Informer<FileDistributeListener>() {
					@Override
					public void inform(FileDistributeListener listener) {
						listener.distributed(result.getFileName());
					}
				});
			}
		}
	}

	/**
	 * TODO : init..
	 * if want handle.. Handler is {@link MessageDispatchRegistry.Handler}
	 */
	private void init() {
		/*MessageDispatchRegistry register = serverDamon.getComponent(ConsoleCommunication.class).getMessageDispatchRegistry();
		register.set(Message.class, new Handler<Message>() {

			@Override
			public void handle(Message message) throws CommunicationException {
				
			}

			@Override
			public void shutdown() {
			}
		});*/
	}

	public interface FileDistributeListener {
		public void distributed(String filename);
	}
}
