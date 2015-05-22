package org.ngrinder.sitemonitor.engine.process;

import java.io.InputStream;

import org.slf4j.LoggerFactory;

import net.grinder.communication.StreamReceiver;
import net.grinder.engine.process.SitemonitorProcess;
import net.grinder.engine.process.WorkerProcessEntryPoint;

import ch.qos.logback.classic.Logger;

/**
 * copied {@link WorkerProcessEntryPoint}
 * modifed for using SitemonitorProcess instead to GrinderProcess
 * 
 * @author Gisoo Gwon
 */
public class SitemonitorProcessEntryPoint {

	public static void main(final String[] args) {
		if (args.length > 1) {
			System.err.println("Usage: java " + SitemonitorProcess.class.getName());
			System.exit(-1);
		}

		final int exitCode = new SitemonitorProcessEntryPoint().run(System.in);

		System.exit(exitCode);
	}

	public int run(InputStream agentCommunicationStream) {
		final Logger logger = (Logger)LoggerFactory.getLogger("worker-bootstrap");

		final SitemonitorProcess sitemonitorProcess;

		try {
			sitemonitorProcess = new SitemonitorProcess(new StreamReceiver(agentCommunicationStream));
		} catch (Exception e) {
			logger.error("Error initialising worker process", e);
			return -2;
		}

		try {
			sitemonitorProcess.run();
			return 0;
		} catch (Exception e) {
			logger.error("Error running worker process", e);
			return -3;
		} catch (Error t) {
			logger.error("Error running worker process", t);
			throw t;
		} finally {
			sitemonitorProcess.shutdown(agentCommunicationStream == System.in);
		}
	}

}
