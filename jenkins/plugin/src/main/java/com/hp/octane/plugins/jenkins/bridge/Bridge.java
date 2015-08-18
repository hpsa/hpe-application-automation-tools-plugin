package com.hp.octane.plugins.jenkins.bridge;

import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.actions.PluginActions;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import org.kohsuke.stapler.export.Exported;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by gullery on 12/08/2015.
 * <p/>
 * This class encompasses functionality of managing connection/s to a single abridged client (MQM Server)
 */

public class Bridge {
	private static final Logger logger = Logger.getLogger(Bridge.class.getName());
	private static int CONCURRENT_CONNECTIONS = 1;

	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5);
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(9);
	private volatile AtomicInteger openedConnections = new AtomicInteger(0);

	private ServerConfiguration mqmConfig;
	private JenkinsMqmRestClientFactory restClientFactory;

	public Bridge(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		this.restClientFactory = clientFactory;
		if (this.mqmConfig.abridged && openedConnections.get() < CONCURRENT_CONNECTIONS) connect();
		logger.info("BRIDGE: new bridge initialized for '" + this.mqmConfig.location + "', state: " + (this.mqmConfig.abridged ? "abridged" : "direct") + " connectivity");
	}

	private void connect() {
		connectivityExecutors.execute(new Runnable() {
			@Override
			public void run() {
				String taskJSON = null;
				try {
					openedConnections.incrementAndGet();
					taskJSON = RESTClientTMP.get(mqmConfig.location +
							"/internal-api/shared_spaces/" + mqmConfig.sharedSpace +
							"/analytics/ci/servers/" + new PluginActions.ServerInfo().getInstanceId() + "/task", null);
					openedConnections.decrementAndGet();
					if (taskJSON != null && !taskJSON.isEmpty()) {
						taskProcessingExecutors.execute(new TaskProcessor(taskJSON));
					}
					//  parse the task, execute and post the result
					connect();
				} catch (Exception e) {
					openedConnections.decrementAndGet();
					logger.severe("connection to MQM Server interrupted: " + e.getMessage());
					connect();
				}
			}
		});
	}

	public void update(ServerConfiguration mqmConfig) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		if (mqmConfig.abridged && openedConnections.get() < CONCURRENT_CONNECTIONS) connect();
		logger.info("BRIDGE: updated for '" + this.mqmConfig.location + "', state: " + (this.mqmConfig.abridged ? "abridged" : "direct") + " connectivity");
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
