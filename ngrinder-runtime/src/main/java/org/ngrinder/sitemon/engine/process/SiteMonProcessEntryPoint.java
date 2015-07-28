package org.ngrinder.sitemon.engine.process;

import java.io.InputStream;

import org.slf4j.LoggerFactory;

import net.grinder.communication.StreamReceiver;
import net.grinder.engine.process.SiteMonProcess;
import net.grinder.engine.process.WorkerProcessEntryPoint;

import ch.qos.logback.classic.Logger;

/**
 * copied {@link WorkerProcessEntryPoint}
 * modifed for using SiteMonProcess instead to GrinderProcess
 * 
 * @author Gisoo Gwon
 */
public class SiteMonProcessEntryPoint {

	public static void main(final String[] args) {
		if (args.length > 1) {
			System.err.println("Usage: java " + SiteMonProcess.class.getName());
			System.exit(-1);
		}

		final int exitCode = new SiteMonProcessEntryPoint().run(System.in);

		System.exit(exitCode);
	}

	public int run(InputStream agentCommunicationStream) {
		final Logger logger = (Logger)LoggerFactory.getLogger("worker-bootstrap");

		final SiteMonProcess siteMonProcess;

		try {
			siteMonProcess = new SiteMonProcess(new StreamReceiver(agentCommunicationStream));
		} catch (Exception e) {
			logger.error("Error initialising worker process", e);
			return -2;
		}

		try {
			siteMonProcess.run();
			return 0;
		} catch (Exception e) {
			logger.error("Error running worker process", e);
			return -3;
		} catch (Error t) {
			logger.error("Error running worker process", t);
			throw t;
		} finally {
			siteMonProcess.shutdown(agentCommunicationStream == System.in);
		}
	}

}
