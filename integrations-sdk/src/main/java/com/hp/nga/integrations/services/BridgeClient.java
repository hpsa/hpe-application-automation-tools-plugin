package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.api.TasksProcessor;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;
import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;
import com.hp.nga.integrations.dto.general.CIServerInfo;
import com.hp.nga.integrations.SDKManager;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by gullery on 12/08/2015.
 * <p/>
 * This class encompasses functionality of managing connection/s to a single abridged client (NGA Server)
 */

final class BridgeClient {
	private static final Logger logger = LogManager.getLogger(BridgeClient.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());
	private final SDKManager sdk;
	volatile private boolean shuttingDown = false;

	BridgeClient(SDKManager sdk) {
		this.sdk = sdk;
		connect();
	}

	private void connect() {
		if (!shuttingDown) {
			connectivityExecutors.execute(new Runnable() {
				public void run() {
					String tasksJSON;
					CIServerInfo serverInfo = sdk.getCIPluginServices().getServerInfo();
					try {
						tasksJSON = getAbridgedTasks(
								serverInfo.getInstanceId(),
								serverInfo.getType().value(),
								serverInfo.getUrl(),
								SDKManager.API_VERSION,
								SDKManager.SDK_VERSION);
						connect();
						if (tasksJSON != null && !tasksJSON.isEmpty()) {
							handleTasks(tasksJSON);
						}
					} catch (Exception e) {
						logger.error("connection to MQM Server temporary failed", e);
						doBreakableWait(1000);
						connect();
					}
				}
			});
		} else if (shuttingDown) {
			logger.info("bridge client stopped");
		}
	}

	private String getAbridgedTasks(String selfIdentity, String selfType, String selfLocation, Integer apiVersion, String sdkVersion) {
		String responseBody = null;
		NGARestClient restClient = sdk.getInternalService(NGARestService.class).obtainClient();
		NGAConfiguration ngaConfiguration = sdk.getCIPluginServices().getNGAConfiguration();
		if (ngaConfiguration != null && ngaConfiguration.isValid()) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("accept", "application/json");
			NGARequest ngaRequest = dtoFactory.newDTO(NGARequest.class)
					.setMethod(NGAHttpMethod.GET)
					.setUrl(ngaConfiguration.getUrl() + "/internal-api/shared_spaces/" +
							ngaConfiguration.getSharedSpace() + "/analytics/ci/servers/" +
							selfIdentity + "/tasks?self-type=" + selfType + "&self-url=" + selfLocation + "&api-version=" + apiVersion + "&sdk-version=" + sdkVersion)
					.setHeaders(headers);
			try {
				NGAResponse ngaResponse = restClient.execute(ngaRequest);
				if (ngaResponse.getStatus() == HttpStatus.SC_OK) {
					responseBody = ngaResponse.getBody();
				} else {
					if (ngaResponse.getStatus() == HttpStatus.SC_REQUEST_TIMEOUT) {
						logger.info("expected timeout disconnection on retrieval of abridged tasks");
					} else if (ngaResponse.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
						logger.error("connection to NGA Server failed: authentication error");
						doBreakableWait(5000);
					} else if (ngaResponse.getStatus() == HttpStatus.SC_FORBIDDEN) {
						logger.error("connection to NGA Server failed: authorization error");
						doBreakableWait(5000);
					} else if (ngaResponse.getStatus() == HttpStatus.SC_NOT_FOUND) {
						logger.error("connection to NGA Server failed: 404, API changes? version problem?");
						doBreakableWait(20000);
					} else {
						logger.info("unexpected response; status: " + ngaResponse.getStatus() + "; content: " + ngaResponse.getBody());
						doBreakableWait(2000);
					}
				}
			} catch (Exception e) {
				logger.error("failed to retrieve abridged tasks", e);
				doBreakableWait(2000);
			}
			return responseBody;
		} else {
			logger.info("NGA is not configured on this plugin, breathing before next retry");
			doBreakableWait(5000);
			return null;
		}
	}

	void dispose() {
		//  TODO: disconnect current connection once async connectivity is possible
		shuttingDown = true;
	}

	private void handleTasks(String tasksJSON) {
		try {
			NGATaskAbridged[] tasks = dtoFactory.dtoCollectionFromJson(tasksJSON, NGATaskAbridged[].class);
			logger.info("going to process " + tasks.length + " tasks");
			for (final NGATaskAbridged task : tasks) {
				taskProcessingExecutors.execute(new Runnable() {
					public void run() {
						TasksProcessor taskProcessor = SDKManager.getService(TasksProcessor.class);
						CIPluginServices pluginServices = sdk.getCIPluginServices();
						NGAResultAbridged result = taskProcessor.execute(task);
						int submitStatus = putAbridgedResult(
								pluginServices.getServerInfo().getInstanceId(),
								result.getId(),
								dtoFactory.dtoToJson(result));
						logger.info("result for task '" + result.getId() + "' submitted with status " + submitStatus);
					}
				});
			}
		} catch (Exception e) {
			logger.error("failed to process tasks", e);
		}
	}

	private int putAbridgedResult(String selfIdentity, String taskId, String contentJSON) {
		NGARestClient restClient = sdk.getInternalService(NGARestService.class).obtainClient();
		NGAConfiguration ngaConfiguration = sdk.getCIPluginServices().getNGAConfiguration();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/json");
		NGARequest ngaRequest = dtoFactory.newDTO(NGARequest.class)
				.setMethod(NGAHttpMethod.PUT)
				.setUrl(ngaConfiguration.getUrl() + "/internal-api/shared_spaces/" + ngaConfiguration.getSharedSpace() + "/analytics/ci/servers/" + selfIdentity + "/tasks/" + taskId + "/result")
				.setHeaders(headers)
				.setBody(contentJSON);
		try {
			NGAResponse ngaResponse = restClient.execute(ngaRequest);
			return ngaResponse.getStatus();
		} catch (IOException ioe) {
			logger.error("failed to submit abridged task's result", ioe);
			return 0;
		}
	}

	//  TODO: turn it to breakable wait with notifier
	private void doBreakableWait(long period) {
		try {
			Thread.sleep(period);
		} catch (InterruptedException ie) {
			logger.warn("interrupted while doing breakable wait");
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
