package com.hp.octane.plugins.jenkins.bridge;

import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.TemporarilyUnavailableException;
import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.services.SDKManager;
import com.hp.nga.integrations.services.TasksProcessor;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by gullery on 12/08/2015.
 * <p>
 * This class encompasses functionality of managing connection/s to a single abridged client (MQM Server)
 */

public class BridgeClient {
	private static final Logger logger = LogManager.getLogger(BridgeClient.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String serverInstanceId = Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity();
	private static final String sdkVersion = Jenkins.getInstance().getPlugin(OctanePlugin.class).getWrapper().getVersion();
	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());
	volatile private boolean shuttingDown = false;

	private ServerConfiguration mqmConfig;
	private JenkinsMqmRestClientFactory restClientFactory;

	public BridgeClient(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password, mqmConfig.impersonatedUser);
		restClientFactory = clientFactory;
		connect();
		logger.info("BRIDGE: client initialized for '" + this.mqmConfig.location + "'; SP: " + this.mqmConfig.sharedSpace + "; access key: " + this.mqmConfig.username);
	}

	public void update(ServerConfiguration newConfig) {
		mqmConfig = new ServerConfiguration(newConfig.location, newConfig.sharedSpace, newConfig.username, newConfig.password, newConfig.impersonatedUser);
		logger.info("BRIDGE: client updated to '" + mqmConfig.location + "'; SP: " + mqmConfig.sharedSpace + "; access key: " + newConfig.username);
		restClientFactory.updateMqmRestClient(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		connect();
	}

	private void connect() {
		if (!shuttingDown) {
			connectivityExecutors.execute(new Runnable() {
				@Override
				public void run() {
					String tasksJSON;
					CIPluginServices pluginServices = SDKManager.getCIPluginServices();
					try {
						MqmRestClient restClient = restClientFactory.obtain(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
						tasksJSON = restClient.getAbridgedTasks(
								serverInstanceId,
								pluginServices.getServerInfo().getUrl(),
								SDKManager.getApiVersion(),
								sdkVersion);
						connect();
						if (tasksJSON != null && !tasksJSON.isEmpty()) {
							dispatchTasks(tasksJSON);
						}
					} catch (AuthenticationException ae) {
						logger.error("BRIDGE: connection to MQM Server temporary failed: authentication error", ae);
						try {
							Thread.sleep(20000);
						} catch (InterruptedException ie) {
							logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
						}
						connect();
					} catch (TemporarilyUnavailableException tue) {
						logger.error("BRIDGE: connection to MQM Server temporary failed: resource not available", tue);
						try {
							Thread.sleep(20000);
						} catch (InterruptedException ie) {
							logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
						}
						connect();
					} catch (Exception e) {
						logger.error("BRIDGE: connection to MQM Server temporary failed: " + e.getMessage(), e);
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
			NGATaskAbridged[] tasks = dtoFactory.dtoCollectionFromJson(tasksJSON, NGATaskAbridged[].class);

			logger.info("BRIDGE: received " + tasks.length + " task(s)");
			for (final NGATaskAbridged task : tasks) {
				taskProcessingExecutors.execute(new Runnable() {
					@Override
					public void run() {
						TasksProcessor TasksProcessor = SDKManager.getTasksProcessor();
						NGAResultAbridged result = TasksProcessor.execute(task);
						MqmRestClient restClient = restClientFactory.obtain(
								mqmConfig.location,
								mqmConfig.sharedSpace,
								mqmConfig.username,
								mqmConfig.password);
						int submitStatus = restClient.putAbridgedResult(
								serverInstanceId,
								result.getId(),
								dtoFactory.dtoToJson(result));
						logger.info("BRIDGE: result for task '" + result.getId() + "' submitted with status " + submitStatus);
					}
				});
			}
		} catch (Exception e) {
			logger.error("BRIDGE: failed to process tasks: " + e.getMessage(), e);
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
