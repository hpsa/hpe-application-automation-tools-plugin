package com.hp.octane.plugins.jenkins.bridge;

import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.actions.PluginActions;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import net.sf.json.JSONArray;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by gullery on 12/08/2015.
 * <p/>
 * This class encompasses functionality of managing connection/s to a single abridged client (MQM Server)
 */

public class BridgeClient {
	private static final Logger logger = Logger.getLogger(BridgeClient.class.getName());
	private static final String serverInstanceId = new PluginActions.ServerInfo().getInstanceId();
	private static int CONCURRENT_CONNECTIONS = 1;

	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());
	private AtomicInteger openedConnections = new AtomicInteger(0);

	private ServerConfiguration mqmConfiguration;
	private JenkinsMqmRestClientFactory restClientFactory;

	public BridgeClient(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory) {
		mqmConfiguration = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password, mqmConfig.impersonatedUser);
		restClientFactory = clientFactory;
		if (this.mqmConfiguration.abridged) connect();
		logger.info("BRIDGE: new bridge initialized for '" + mqmConfiguration.location + "' (SP: " + mqmConfiguration.sharedSpace + "), state: " + (mqmConfiguration.abridged ? "abridged" : "direct") + " connectivity");
	}

	public void update(ServerConfiguration mqmConfig) {
		mqmConfiguration = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password, mqmConfig.impersonatedUser);
		if (mqmConfig.abridged && openedConnections.get() < CONCURRENT_CONNECTIONS) connect();
		logger.info("BRIDGE: updated for '" + mqmConfiguration.location + "' (SP: " + mqmConfiguration.sharedSpace + "), state: " + (mqmConfiguration.abridged ? "abridged" : "direct") + " connectivity");
	}

	private void connect() {
		connectivityExecutors.execute(new Runnable() {
			@Override
			public void run() {
				String tasksJSON;
				int totalConnections;
				try {
					totalConnections = openedConnections.incrementAndGet();
					logger.info("BRIDGE: connecting to '" + mqmConfiguration.location + "' (SP: " + mqmConfiguration.sharedSpace + ")...; total connections [including new one]: " + totalConnections);
					MqmRestClient restClient = restClientFactory.create(
							mqmConfiguration.location,
							mqmConfiguration.sharedSpace,
							mqmConfiguration.username,
							mqmConfiguration.password);
					tasksJSON = restClient.getAbridgedTasks(serverInstanceId, new PluginActions.ServerInfo().getUrl());
					logger.info("BRIDGE: back from '" + mqmConfiguration.location + "' (SP: " + mqmConfiguration.sharedSpace + ") with " + (tasksJSON == null || tasksJSON.isEmpty() ? "no tasks" : "some tasks"));
					openedConnections.decrementAndGet();
					if (mqmConfiguration.abridged && openedConnections.get() < CONCURRENT_CONNECTIONS) {
						connect();
					}
					if (tasksJSON != null && !tasksJSON.isEmpty()) {
						dispatchTasks(tasksJSON);
					}
				} catch (AuthenticationException ae) {
					openedConnections.decrementAndGet();
					logger.severe("BRIDGE: connection to MQM Server temporary failed: authentication error");
					try {
						Thread.sleep(20000);
					} catch (InterruptedException ie) {
						logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
					}
					if (mqmConfiguration.abridged && openedConnections.get() < CONCURRENT_CONNECTIONS) {
						connect();
					}
				} catch (Exception e) {
					openedConnections.decrementAndGet();
					logger.severe("BRIDGE: connection to MQM Server temporary failed: " + e.getMessage());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
					}
					if (mqmConfiguration.abridged && openedConnections.get() < CONCURRENT_CONNECTIONS) {
						connect();
					}
				}
			}
		});
	}

	private void dispatchTasks(String tasksJSON) {
		try {
			JSONArray tasks = JSONArray.fromObject(tasksJSON);
			logger.info("BRIDGE: going to process " + tasks.size() + " tasks");
			for (int i = 0; i < tasks.size(); i++) {
				taskProcessingExecutors.execute(new TaskProcessor(
						tasks.getJSONObject(i),
						restClientFactory,
						mqmConfiguration
				));
			}
		} catch (Exception e) {
			logger.severe("BRIDGE: failed to process tasks: " + e.getMessage());
		}
	}

	@Exported(inline = true)
	public String getLocation() {
		return mqmConfiguration.location;
	}

	@Exported(inline = true)
	public String getSharedSpace() {
		return mqmConfiguration.sharedSpace;
	}

	@Exported(inline = true)
	public String getUsername() {
		return mqmConfiguration.username;
	}

	private static final class AbridgedConnectivityExecutorsFactory implements ThreadFactory {

		@Override
		public Thread newThread(@Nonnull Runnable runnable) {
			Thread result = new Thread(runnable);
			result.setName("AbridgedConnectivityThread-" + result.getId());
			result.setDaemon(true);
			return result;
		}
	}

	private static final class AbridgedTasksExecutorsFactory implements ThreadFactory {

		@Override
		public Thread newThread(@Nonnull Runnable runnable) {
			Thread result = new Thread(runnable);
			result.setName("AbridgedTasksExecutorsFactory-" + result.getId());
			result.setDaemon(true);
			return result;
		}
	}
}
