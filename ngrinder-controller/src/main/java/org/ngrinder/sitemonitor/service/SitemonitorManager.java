package org.ngrinder.sitemonitor.service;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.sitemonitor.SitemonitorControllerServerDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.grinder.common.processidentity.AgentIdentity;

/**
 * @author Gisoo Gwon
 */
@Component
public class SitemonitorManager implements ControllerConstants {

	public static final Logger LOGGER = LoggerFactory.getLogger(SitemonitorManager.class);
	private SitemonitorControllerServerDaemon sitemonitorServerDaemon;

	@Autowired
	private Config config;

	/**
	 * Initialize sitemonitor manager.
	 */
	@PostConstruct
	public void init() {
		int port = config.getSitemonitorControllerPort();
		sitemonitorServerDaemon = new SitemonitorControllerServerDaemon(port);
		sitemonitorServerDaemon.start();
	}

	/**
	 * Shutdown sitemonitor controller server.
	 */
	@PreDestroy
	public void destroy() {
		sitemonitorServerDaemon.shutdown();
	}

	/**
	 * @return 
	 */
	public Set<AgentIdentity> getAllAgent() {
		return sitemonitorServerDaemon.getAllAvailableAgents();
	}
}