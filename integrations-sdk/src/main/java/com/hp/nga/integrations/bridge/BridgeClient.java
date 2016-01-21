package com.hp.nga.integrations.bridge;

import com.hp.nga.integrations.configuration.ServerConfiguration;
import com.hp.nga.integrations.api.CIDataProvider;
import com.hp.nga.integrations.dto.rest.AbridgedTask;
import com.hp.nga.integrations.serialization.SerializationService;

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
	private static final String serverInstanceId = CIDataProvider.getInstance().getServerInfo().getInstanceId();

	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());
	volatile private boolean shuttingDown = false;

	public BridgeClient() {
		connect();
//		logger.info("BRIDGE: client initialized for '" + this.config.getUrl() + "' (SP: " + this.config.getSharedSpace() + ")");
	}

	public void update(ServerConfiguration newConfig) {
//		logger.info("BRIDGE: updated for '" + config.getUrl() + "' (SP: " + config.getSharedSpace() + ")");
		connect();
	}

	private void connect() {
		if (!shuttingDown) {
			connectivityExecutors.execute(new Runnable() {
				public void run() {
					String tasksJSON = "";
					try {
//						logger.info("BRIDGE: connecting to '" + config.getUrl() +
//								"' (SP: " + config.getSharedSpace() +
//								"; instance ID: " + serverInstanceId +
//								"; own URL: " + CIDataProvider.getInstance().getServerInfo().getUrl());
//						MqmRestClient restClient = restClientFactory.create(config.getUrl(), config.getSharedSpace(), config.getUsername(), config.getPassword());
//						tasksJSON = restClient.getAbridgedTasks(serverInstanceId, new PluginActions.ServerInfo().getUrl());
//						logger.info("BRIDGE: back from '" + config.getUrl() + "' (SP: " + config.getSharedSpace() + ") with " + (tasksJSON == null || tasksJSON.isEmpty() ? "no tasks" : "some tasks"));
						connect();
						if (tasksJSON != null && !tasksJSON.isEmpty()) {
							dispatchTasks(tasksJSON);
						}
//					} catch (AuthenticationException ae) {
//						logger.severe("BRIDGE: connection to MQM Server temporary failed: authentication error");
//						try {
//							Thread.sleep(20000);
//						} catch (InterruptedException ie) {
//							logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
//						}
//						connect();
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
			AbridgedTask[] tasks = SerializationService.fromJSON(tasksJSON, AbridgedTask[].class);

			logger.info("BRIDGE: going to process " + tasks.length + " tasks");
			for (AbridgedTask task : tasks) {
				//taskProcessingExecutors.execute(new TaskProcessor(task));
			}
		} catch (Exception e) {
			logger.severe("BRIDGE: failed to process tasks: " + e.getMessage());
		}
	}

	private static final class AbridgedConnectivityExecutorsFactory implements ThreadFactory {
		public Thread newThread(Runnable runnable) {
			Thread result = new Thread(runnable);
			result.setName("AbridgedConnectivityThread-" + result.getId());
			result.setDaemon(true);
			return result;
		}
	}

	private static final class AbridgedTasksExecutorsFactory implements ThreadFactory {
		public Thread newThread(Runnable runnable) {
			Thread result = new Thread(runnable);
			result.setName("AbridgedTasksExecutorsFactory-" + result.getId());
			result.setDaemon(true);
			return result;
		}
	}
}
