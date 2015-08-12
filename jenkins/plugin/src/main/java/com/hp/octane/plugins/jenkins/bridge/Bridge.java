package com.hp.octane.plugins.jenkins.bridge;

import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import org.kohsuke.stapler.export.Exported;

import java.util.logging.Logger;

/**
 * Created by gullery on 12/08/2015.
 * <p/>
 * This class encompasses functionality managing connection to a single abridged client (server)
 */

public class Bridge {
	private static final Logger logger = Logger.getLogger(Bridge.class.getName());

	private final Object initLocker = new Object();
	private Thread worker;
	private boolean shuttingDown;

	private ServerConfiguration mqmConfig;
	private JenkinsMqmRestClientFactory restClientFactory;

	public Bridge(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		this.restClientFactory = clientFactory;
		activate();
		logger.info("BRIDGE: new bridge initialized for '" + this.mqmConfig.location + "'");
	}

	void activate() {
		shuttingDown = false;
		if (worker == null || !worker.isAlive()) {
			synchronized (initLocker) {
				if (worker == null || !worker.isAlive()) {
					worker = new Thread(new Runnable() {
						@Override
						public void run() {
							while (!shuttingDown) {
								try {
									//  do the actual call to
									//  yet grab another thread and send new connection immediately
								} catch (Exception e) {
									logger.severe("BRIDGE: Exception while bridge connection: " + e.getMessage());
								}
							}
							logger.info("BRIDGE: worker thread of bridge shuts down");
						}
					});
					worker.setDaemon(true);
					worker.setName("BridgeWorker");
					worker.start();
				}
			}
		}
	}

	public void update(ServerConfiguration mqmConfig) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		logger.info("BRIDGE: updated for '" + this.mqmConfig.location + "'");
	}

	@Exported(inline = true)
	public String getLocation() {
		return mqmConfig.location;
	}

	@Exported(inline = true)
	public String getSharedSpace() {
		return mqmConfig.sharedSpace;
	}

	@Exported(inline = true)
	public String getUsername() {
		return mqmConfig.username;
	}
}
