/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.octane.bridge;

import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.ServerException;
import com.hp.mqm.client.exception.TemporarilyUnavailableException;
import com.hp.mqm.client.model.AbridgedTaskPluginInfo;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.spi.CIPluginServices;
import com.hp.octane.integrations.api.TasksProcessor;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.connectivity.OctaneTaskAbridged;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hpe.application.automation.tools.octane.configuration.ServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class BridgeClient {
	private static final Logger logger = LogManager.getLogger(BridgeClient.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static String serverInstanceId;
	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());
	volatile private boolean isConnected = false;
	volatile private boolean shuttingDown = false;

	private ServerConfiguration mqmConfig;
	private JenkinsMqmRestClientFactory restClientFactory;

	public BridgeClient(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory, String serverIdentity) {
		this.serverInstanceId = serverIdentity;
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password, mqmConfig.impersonatedUser);
		restClientFactory = clientFactory;
		connect();
		logger.info("client initialized for '" + this.mqmConfig.location + "'; SP: " + this.mqmConfig.sharedSpace + "; access key: " + this.mqmConfig.username);
	}

	public void update(ServerConfiguration newConfig, String serverIdentity) {
		this.serverInstanceId = serverIdentity;
		mqmConfig = new ServerConfiguration(newConfig.location, newConfig.sharedSpace, newConfig.username, newConfig.password, newConfig.impersonatedUser);
		logger.info("client updated to '" + mqmConfig.location + "'; SP: " + mqmConfig.sharedSpace + "; access key: " + newConfig.username);
		restClientFactory.updateMqmRestClient(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		connect();
	}

	private void connect() {
		if (!shuttingDown && !isConnected) {
			isConnected = true;
			connectivityExecutors.execute(new Runnable() {
				@Override
				public void run() {
					String tasksJSON;
					CIPluginServices pluginServices = OctaneSDK.getInstance().getPluginServices();
					try {
						MqmRestClient restClient = restClientFactory.obtain(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
						AbridgedTaskPluginInfo info = new AbridgedTaskPluginInfo()
								.setSelfIdentity(serverInstanceId)
								.setSelfType(pluginServices.getServerInfo().getType().value())
								.setSelfLocation(pluginServices.getServerInfo().getUrl())
								.setApiVersion(OctaneSDK.API_VERSION)
								.setSdkVersion(OctaneSDK.SDK_VERSION)
								.setPluginVersion(OctaneSDK.getInstance().getPluginServices().getPluginInfo().getVersion())
								.setOctaneUser(pluginServices.getOctaneConfiguration().getApiKey())
								.setCiServerUser(ConfigurationService.getModel().getImpersonatedUser());
						tasksJSON = restClient.getAbridgedTasks(info);
						isConnected = false;
						connect();
						if (tasksJSON != null && !tasksJSON.isEmpty()) {
							dispatchTasks(tasksJSON);
						}
					} catch (AuthenticationException ae) {
						isConnected = false;
						logger.error("connection to MQM Server temporary failed: authentication error", ae);
						try {
							Thread.sleep(20000);
						} catch (InterruptedException ie) {
							logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
						}
						connect();
					} catch (TemporarilyUnavailableException tue) {
						isConnected = false;
						logger.error("connection to MQM Server temporary failed: resource not available", tue);
						try {
							Thread.sleep(20000);
						} catch (InterruptedException ie) {
							logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
						}
						connect();
					} catch (ServerException se) {
						isConnected = false;
						logger.error("connection to MQM Server temporary failed: " + se.getMessage(), se);
						try {
							Thread.sleep(10000);
						} catch (InterruptedException ie) {
							logger.info("interrupted while breathing on temporary exception, continue to re-connect...");
						}
						connect();
					} catch (Exception e) {
						isConnected = false;
						logger.error("connection to MQM Server temporary failed: " + e.getMessage(), e);
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
			logger.info("bridge client stopped");
		}
	}

	void dispose() {
		//  TODO: disconnect current connection once async connectivity is possible
		shuttingDown = true;
	}

	private void dispatchTasks(String tasksJSON) {
		try {
			OctaneTaskAbridged[] tasks = dtoFactory.dtoCollectionFromJson(tasksJSON, OctaneTaskAbridged[].class);

			logger.info("received " + tasks.length + " task(s)");
			for (final OctaneTaskAbridged task : tasks) {
				taskProcessingExecutors.execute(new Runnable() {
					@Override
					public void run() {
						try {
							TasksProcessor TasksProcessor = OctaneSDK.getInstance().getTasksProcessor();
							OctaneResultAbridged result = TasksProcessor.execute(task);
							MqmRestClient restClient = restClientFactory.obtain(
									mqmConfig.location,
									mqmConfig.sharedSpace,
									mqmConfig.username,
									mqmConfig.password);

							int submitStatus = restClient.putAbridgedResult(
								serverInstanceId,
								result.getId(),
								dtoFactory.dtoToJson(result));
							logger.info("result for task '" + result.getId() + "' submitted with status " + submitStatus);
						} catch (Exception e) {
							logger.error("failed to submit task '" + task.getId(), e);
						}
					}
				});
			}
		} catch (Exception e) {
			logger.error("failed to process tasks: " + e.getMessage(), e);
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
