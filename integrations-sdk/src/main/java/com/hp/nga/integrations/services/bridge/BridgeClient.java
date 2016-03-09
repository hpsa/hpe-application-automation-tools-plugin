package com.hp.nga.integrations.services.bridge;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;
import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;
import com.hp.nga.integrations.dto.general.CIServerInfo;
import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.services.tasking.TasksProcessor;
import com.hp.nga.integrations.services.rest.NGARestClient;
import com.hp.nga.integrations.services.rest.NGARestServiceImpl;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

class BridgeClient {
	private static final Logger logger = LogManager.getLogger(BridgeClient.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());
	volatile private boolean shuttingDown = false;

	BridgeClient() {
		connect();
	}

	private void connect() {
		if (!shuttingDown) {
			connectivityExecutors.execute(new Runnable() {
				public void run() {
					String tasksJSON;
					CIServerInfo serverInfo = SDKManager.getCIPluginServices().getServerInfo();
					try {
						NGARestClient restClient = NGARestServiceImpl.getInstance().obtainClient();
						tasksJSON = getAbridgedTasks(
								serverInfo.getInstanceId(),
								serverInfo.getUrl(),
								SDKManager.getAPIVersion(),
								SDKManager.getSDKVersion(),
								restClient);
						connect();
						if (tasksJSON != null && !tasksJSON.isEmpty()) {
							handleTasks(tasksJSON);
						}
					} catch (Exception e) {
						logger.error("connection to MQM Server temporary failed", e);
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

	private String getAbridgedTasks(String selfIdentity, String selfLocation, Integer apiVersion, String sdkVersion, NGARestClient restClient) {
		String responseBody = null;
		NGAConfiguration ngaConfiguration = SDKManager.getCIPluginServices().getNGAConfiguration();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("accept", "application/json");
		NGARequest ngaRequest = dtoFactory.newDTO(NGARequest.class)
				.setMethod(NGAHttpMethod.GET)
				.setUrl(ngaConfiguration.getUrl() + "/internal-api/shared_spaces/" + ngaConfiguration.getSharedSpace() + "/analytics/ci/servers/" + selfIdentity + "/tasks?self-url=" + selfLocation + "&api-version=" + apiVersion + "&sdk-version=" + sdkVersion)
				.setHeaders(headers);
		try {
			NGAResponse ngaResponse = restClient.execute(ngaRequest);
			if (ngaResponse.getStatus() == HttpStatus.SC_OK) {
				responseBody = ngaResponse.getBody();
			} else {
				if (ngaResponse.getStatus() == HttpStatus.SC_REQUEST_TIMEOUT) {
					logger.info("expected timeout disconnection on retrieval of abridged tasks");
				} else if (ngaResponse.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
					//  TODO: this is a 'fatal' error since relogin didn't help and only configuration change may solve it; still need to continue to retry, maybe breathe more
				} else if (ngaResponse.getStatus() == HttpStatus.SC_NOT_FOUND) {
					//  TODO: thei is a 'fatal' error since NGA server changed an API signature and only redeploy/rebuild may help; still need to continue to retry, maybe breathe more
				} else {
					logger.info("unexpected response; status: " + ngaResponse.getStatus() + "; content: " + ngaResponse.getBody());
				}
			}
		} catch (Exception e) {
			logger.error("failed to retrieve abridged tasks", e);
		}
		return responseBody;
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
						TasksProcessor taskProcessor = SDKManager.getTasksProcessor();
						CIPluginServices pluginServices = SDKManager.getCIPluginServices();
						NGAResultAbridged result = taskProcessor.execute(task);
						NGARestClient restClient = NGARestServiceImpl.getInstance().obtainClient();
						int submitStatus = putAbridgedResult(
								pluginServices.getServerInfo().getInstanceId(),
								result.getId(),
								dtoFactory.dtoToJson(result),
								restClient);
						logger.info("result for task '" + result.getId() + "' submitted with status " + submitStatus);
					}
				});
			}
		} catch (Exception e) {
			logger.error("failed to process tasks", e);
		}
	}

	private int putAbridgedResult(String selfIdentity, String taskId, String contentJSON, NGARestClient restClient) {
		NGAConfiguration ngaConfiguration = SDKManager.getCIPluginServices().getNGAConfiguration();
		NGARequest ngaRequest = dtoFactory.newDTO(NGARequest.class)
				.setUrl(ngaConfiguration.getUrl() + "/internal-api/shared_spaces/" + ngaConfiguration.getSharedSpace() + "/analytics/ci/servers/" + selfIdentity + "/tasks/" + taskId + "/result")
				.setMethod(NGAHttpMethod.PUT)
				.setBody(contentJSON);
		try {
			NGAResponse ngaResponse = restClient.execute(ngaRequest);
			return ngaResponse.getStatus();
		} catch (Exception e) {
			logger.error("failed to submit abridged task's result", e);
			throw new RuntimeException(e);
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
