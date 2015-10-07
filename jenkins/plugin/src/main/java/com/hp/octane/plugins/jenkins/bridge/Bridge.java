package com.hp.octane.plugins.jenkins.bridge;

import com.hp.octane.plugins.jenkins.actions.PluginActions;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30);
	private volatile AtomicInteger openedConnections = new AtomicInteger(0);

	private ServerConfiguration mqmConfig;
	private JenkinsMqmRestClientFactory restClientFactory;

	public Bridge(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password, mqmConfig.impersonatedUser);
		this.restClientFactory = clientFactory;
		if (this.mqmConfig.abridged) connect();
		logger.info("BRIDGE: new bridge initialized for '" + this.mqmConfig.location + "', state: " + (this.mqmConfig.abridged ? "abridged" : "direct") + " connectivity");
	}

	public void update(ServerConfiguration mqmConfig) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password, mqmConfig.impersonatedUser);
		if (mqmConfig.abridged && openedConnections.get() < CONCURRENT_CONNECTIONS) connect();
		logger.info("BRIDGE: updated for '" + this.mqmConfig.location + "', state: " + (this.mqmConfig.abridged ? "abridged" : "direct") + " connectivity");
	}

	private void connect() {
		connectivityExecutors.execute(new Runnable() {
			@Override
			public void run() {
				String taskJSON;
				int totalConnections;
				try {
					totalConnections = openedConnections.incrementAndGet();
					logger.info("BRIDGE: connecting to '" + mqmConfig.location + "'...; total connections [including new one]: " + totalConnections);
					taskJSON = RESTClientTMP.getTask(mqmConfig.location +
							"/internal-api/shared_spaces/" + mqmConfig.sharedSpace +
							"/analytics/ci/servers/" + new PluginActions.ServerInfo().getInstanceId() + "/task", null);
					logger.info("BRIDGE: back from '" + mqmConfig.location + "' with " + (taskJSON == null || taskJSON.isEmpty() ? "no tasks" : "some tasks"));
					openedConnections.decrementAndGet();
					if (mqmConfig.abridged && openedConnections.get() < CONCURRENT_CONNECTIONS) {
						connect();
					}
					dispatchTasks(taskJSON);
				} catch (RESTClientTMP.TemporaryException te) {
					openedConnections.decrementAndGet();
					logger.severe("BRIDGE: connection to MQM Server temporary failed: " + te.getMessage());
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
					}
					if (mqmConfig.abridged && openedConnections.get() < CONCURRENT_CONNECTIONS) {
						connect();
					}
				} catch (RESTClientTMP.FatalException fe) {
					openedConnections.decrementAndGet();
					logger.severe("BRIDGE: connection to MQM Server fatally failed: " + fe.getMessage());
				}
			}
		});
	}

	private void dispatchTasks(String tasksJSON) {
		if (tasksJSON != null && !tasksJSON.isEmpty()) {
			try {
				JSONArray tasks = JSONArray.fromObject(tasksJSON);
				logger.info("BRIDGE: going to process " + tasks.size() + " tasks");
				for (int i = 0; i < tasks.size(); i++) {
					taskProcessingExecutors.execute(new TaskProcessor(
							tasks.getJSONObject(i),
							mqmConfig.location + "/internal-api/shared_spaces/" + mqmConfig.sharedSpace + "/analytics/ci/servers/" + new PluginActions.ServerInfo().getInstanceId() + "/task"
					));
				}
			} catch (Exception e) {
				logger.severe("BRIDGE: failed to process tasks: " + e.getMessage());
			}
		}
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
