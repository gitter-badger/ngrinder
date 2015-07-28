package org.ngrinder.sitemon;

import java.util.Set;
import java.util.Timer;
import java.util.regex.Pattern;

import org.python.google.common.base.Predicate;

import net.grinder.AgentControllerServerDaemon;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.Address;
import net.grinder.communication.Message;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.communication.DistributionControl;
import net.grinder.console.communication.DistributionControlImplementationAddressDecorator;
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
public class SiteMonControllerServerDaemon {
	private AgentControllerServerDaemon serverDaemon;
	private final Timer timer;

	public SiteMonControllerServerDaemon(int port) {
		serverDaemon = new AgentControllerServerDaemon(port);
		timer = new Timer();
	}

	public void start() {
		serverDaemon.start();
	}
	
	public void shutdown() {
		serverDaemon.shutdown();
	}

	public void sendToAgents(Message message) {
		ConsoleCommunication consoleCommunication = serverDaemon.getComponent(ConsoleCommunication.class);
		consoleCommunication.sendToAgents(message);
	}

	public void sendToAddressedAgents(Address address, Message message) {
		ConsoleCommunication consoleCommunication = serverDaemon.getComponent(ConsoleCommunication.class);
		consoleCommunication.sendToAddressedAgents(address, message);
	}

	public Set<AgentIdentity> getAllAvailableAgents() {
		return serverDaemon.getComponent(AgentProcessControlImplementation.class).getAllAgents();
	}

	public Set<AgentStatus> getAllAgentStatus() {
		return serverDaemon.getComponent(AgentProcessControlImplementation.class).getAgentStatusSet(
			new Predicate<AgentProcessControlImplementation.AgentStatus>() {
				@Override
				public boolean apply(AgentStatus arg0) {
					return true;
				}
			});
	}

	public void sendFile(Address address, Directory directory, Pattern fileFilterPattern,
		ListenerSupport<FileDistributeListener> listener) throws FileContentsException {

		ConsoleCommunication consoleCommunication = serverDaemon.getComponent(ConsoleCommunication.class);
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

	public interface FileDistributeListener {
		public void distributed(String filename);
	}

	/**
	 * @return use port
	 */
	public int getPort() {
		return serverDaemon.getPort();
	}

	/**
	 * Get component used in {@link AgentControllerServerDaemon}.
	 *
	 * @param componentType component type class
	 * @param <T>           component type class
	 * @return <T> the component in consoleFoundation
	 */
	public <T> T getComponent(Class<T> componentType) {
		return serverDaemon.getComponent(componentType);
	}
}
