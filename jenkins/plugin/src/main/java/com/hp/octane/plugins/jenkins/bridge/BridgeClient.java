package com.hp.octane.plugins.jenkins.bridge;

import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.TemporarilyUnavailableException;
import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.services.SDKFactory;
import com.hp.nga.integrations.services.bridge.NGATaskProcessor;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;
import com.hp.nga.integrations.services.serialization.SerializationService;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

/**
 * Created by gullery on 12/08/2015.
 * <p>
 * This class encompasses functionality of managing connection/s to a single abridged client (MQM Server)
 */

public class BridgeClient {
	private static final Logger logger = Logger.getLogger(BridgeClient.class.getName());
	private static final String serverInstanceId = Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity();
	private static final String pluginVersion = Jenkins.getInstance().getPlugin(OctanePlugin.class).getWrapper().getVersion();
	private static final Integer apiVersion = 1;
	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());
	volatile private boolean shuttingDown = false;

	private ServerConfiguration mqmConfig;
	private JenkinsMqmRestClientFactory restClientFactory;

	public BridgeClient(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password, mqmConfig.impersonatedUser);
		restClientFactory = clientFactory;
		connect();
		logger.info("BRIDGE: client initialized for '" + this.mqmConfig.location + "' (SP: " + this.mqmConfig.sharedSpace + ")");
	}

	public void update(ServerConfiguration newConfig) {
		mqmConfig = new ServerConfiguration(newConfig.location, newConfig.sharedSpace, newConfig.username, newConfig.password, newConfig.impersonatedUser);
		logger.info("BRIDGE: updated for '" + mqmConfig.location + "' (SP: " + mqmConfig.sharedSpace + ")");
		restClientFactory.updateMqmRestClient(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		connect();
	}

	private void connect() {
		if (!shuttingDown) {
			connectivityExecutors.execute(new Runnable() {
				@Override
				public void run() {
					String tasksJSON;
					CIPluginServices pluginServices = SDKFactory.getCIPluginServices();
					try {
						//  TODO: get server self URL by means of CIPluginServices
						logger.info("BRIDGE: connecting to '" + mqmConfig.location +
								"' (SP: " + mqmConfig.sharedSpace +
								"; instance ID: " + pluginServices.getServerInfo().getInstanceId() +
								"; self URL: " + "<to be implemented>");
						MqmRestClient restClient = restClientFactory.obtain(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
						tasksJSON = restClient.getAbridgedTasks(serverInstanceId, "http://dummy/url", pluginVersion, apiVersion);
						logger.info("BRIDGE: back from '" + mqmConfig.location + "' (SP: " + mqmConfig.sharedSpace + ") with " + (tasksJSON == null || tasksJSON.isEmpty() ? "no tasks" : "some tasks"));
						connect();
						if (tasksJSON != null && !tasksJSON.isEmpty()) {
							dispatchTasks(tasksJSON);
						}
					} catch (AuthenticationException ae) {
						logger.severe("BRIDGE: connection to MQM Server temporary failed: authentication error");
						try {
							Thread.sleep(20000);
						} catch (InterruptedException ie) {
							logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
						}
						connect();
					} catch (TemporarilyUnavailableException tue) {
						logger.severe("BRIDGE: connection to MQM Server temporary failed: resource not available");
						try {
							Thread.sleep(20000);
						} catch (InterruptedException ie) {
							logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
						}
						connect();
					} catch (Exception e) {
						logger.severe("BRIDGE: connection to MQM Server temporary failed: " + e.getMessage());
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
							logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
						}
						connect();
					}
				}
			});
		} else if (shuttingDown) {
			logger.info("BRIDGE: bridge client stopped");
		}
	}

	void dispose() {
		//  TODO: disconnect current connection once async connectivity is possible
		shuttingDown = true;
	}

	private void dispatchTasks(String tasksJSON) {
		try {
			NGATaskAbridged[] tasks = SerializationService.fromJSON(tasksJSON, NGATaskAbridged[].class);
			logger.info("BRIDGE: going to process " + tasks.length + " tasks");
			for (final NGATaskAbridged task : tasks) {
				taskProcessingExecutors.execute(new Runnable() {
					@Override
					public void run() {
						NGATaskProcessor NGATaskProcessor = new NGATaskProcessor(task);
						NGAResultAbridged result = NGATaskProcessor.execute();
						MqmRestClient restClient = restClientFactory.obtain(
								mqmConfig.location,
								mqmConfig.sharedSpace,
								mqmConfig.username,
								mqmConfig.password);
						JSONObject json = new JSONObject();
						json.put("statusCode", result.getStatus());
						json.put("headers", result.getHeaders());
						json.put("body", result.getBody());

						int submitStatus = restClient.putAbridgedResult(
								serverInstanceId,
								result.getId(),
								json.toString());
						logger.info("BRIDGE: result for task '" + result.getId() + "' submitted with status " + submitStatus);
					}
				});
			}
		} catch (Exception e) {
			logger.severe("BRIDGE: failed to process tasks: " + e.getMessage());
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
