package com.hp.nga.integrations.services.bridge;

import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.rest.NGAResult;
import com.hp.nga.integrations.dto.rest.NGATask;
import com.hp.nga.integrations.services.serialization.SerializationService;

import java.net.MalformedURLException;
import java.net.URL;
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

	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());
	volatile private boolean shuttingDown = false;

	public BridgeClient() {
		connect();
//		logger.info("BRIDGE: client initialized for '" + this.config.getUrl() + "' (SP: " + this.config.getSharedSpace() + ")");
	}

	public void update(NGAConfiguration newConfig) {
//		logger.info("BRIDGE: updated for '" + config.getUrl() + "' (SP: " + config.getSharedSpace() + ")");
		if(isConfigurationValid(newConfig)){
			//TODO: 1. update the rest client
			connect();
		}else{
			/*
				logger.info("BRIDGE: empty / non-valid configuration submitted, disposing bridge client");
				bridgeClient.dispose();
				bridgeClient = null;
			* */
		}
	}

	private boolean isConfigurationValid(NGAConfiguration serverConfiguration) {
		boolean result = false;
		if (serverConfiguration.getUrl() != null && !serverConfiguration.getUrl().isEmpty() &&
				serverConfiguration.getSharedSpace() != null /*&&!serverConfiguration.getSharedSpace().isEmpty()*/) {
			try {
				URL tmp = new URL(serverConfiguration.getUrl());
				result = true;
			} catch (MalformedURLException mue) {
				logger.warning("BRIDGE: configuration with malformed URL supplied");
			}
		}
		return result;
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
							handleTasks(tasksJSON);
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

	private void handleTasks(String tasksJSON) {
		try {
			NGATask[] tasks = SerializationService.fromJSON(tasksJSON, NGATask[].class);

			logger.info("BRIDGE: going to process " + tasks.length + " tasks");
			for (final NGATask task : tasks) {
				taskProcessingExecutors.execute(new Runnable() {
					public void run() {
						NGATaskProcessor taskProcessor = new NGATaskProcessor(task);
						NGAResult result = taskProcessor.execute();
						//  TODO: post the result to NGA
					}
				});
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
