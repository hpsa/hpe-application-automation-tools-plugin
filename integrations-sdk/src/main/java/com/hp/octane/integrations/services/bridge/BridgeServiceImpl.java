package com.hp.octane.integrations.services.bridge;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.api.RestClient;
import com.hp.octane.integrations.api.RestService;
import com.hp.octane.integrations.api.TasksProcessor;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.api.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.api.connectivity.HttpMethod;
import com.hp.octane.integrations.dto.api.connectivity.OctaneRequest;
import com.hp.octane.integrations.dto.api.connectivity.OctaneResponse;
import com.hp.octane.integrations.dto.api.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.api.connectivity.OctaneTaskAbridged;
import com.hp.octane.integrations.dto.api.general.CIServerInfo;
import com.hp.octane.integrations.spi.CIPluginServices;
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
 * Created by gullery on 05/08/2015.
 * <p/>
 * Bridge Service meant to provide an abridged connectivity functionality
 */

public final class BridgeServiceImpl extends OctaneSDK.SDKServiceBase {
	private static final Logger logger = LogManager.getLogger(BridgeServiceImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private ExecutorService connectivityExecutors = Executors.newFixedThreadPool(5, new AbridgedConnectivityExecutorsFactory());
	private ExecutorService taskProcessingExecutors = Executors.newFixedThreadPool(30, new AbridgedTasksExecutorsFactory());

	private final CIPluginServices pluginServices;
	private final RestService restService;
	private final TasksProcessor tasksProcessor;

	public BridgeServiceImpl(Object configurator, CIPluginServices pluginServices, RestService restService, TasksProcessor tasksProcessor, boolean initBridge) {
		super(configurator);

		if (pluginServices == null) {
			throw new IllegalArgumentException("plugin services MUST NOT be null");
		}
		if (restService == null) {
			throw new IllegalArgumentException("rest service MUST NOT be null");
		}
		if (tasksProcessor == null) {
			throw new IllegalArgumentException("task processor MUST NOT be null");
		}

		this.pluginServices = pluginServices;
		this.restService = restService;
		this.tasksProcessor = tasksProcessor;
		if (initBridge) {
			connect();
		}
	}

	private void connect() {
		if (!connectivityExecutors.isShutdown()) {
			connectivityExecutors.execute(new Runnable() {
				public void run() {
					String tasksJSON;
					CIServerInfo serverInfo = pluginServices.getServerInfo();
					try {
						tasksJSON = getAbridgedTasks(
								serverInfo.getInstanceId(),
								serverInfo.getType().value(),
								serverInfo.getUrl(),
								OctaneSDK.API_VERSION,
								OctaneSDK.SDK_VERSION);
						connect();
						if (tasksJSON != null && !tasksJSON.isEmpty()) {
							handleTasks(tasksJSON);
						}
					} catch (Exception e) {
						logger.error("connection to Octane Server temporary failed", e);
						doBreakableWait(1000);
						connect();
					}
				}
			});
		} else {
			logger.info("bridge service stopped gracefully by external request");
		}
	}

	private String getAbridgedTasks(String selfIdentity, String selfType, String selfLocation, Integer apiVersion, String sdkVersion) {
		String responseBody = null;
		RestClient restClient = restService.obtainClient();
		OctaneConfiguration octaneConfiguration = pluginServices.getOctaneConfiguration();
		if (octaneConfiguration != null && octaneConfiguration.isValid()) {
			Map<String, String> headers = new HashMap<>();
			headers.put("accept", "application/json");
			OctaneRequest octaneRequest = dtoFactory.newDTO(OctaneRequest.class)
					.setMethod(HttpMethod.GET)
					.setUrl(octaneConfiguration.getUrl() + "/internal-api/shared_spaces/" +
							octaneConfiguration.getSharedSpace() + "/analytics/ci/servers/" +
							selfIdentity + "/tasks?self-type=" + selfType + "&self-url=" + selfLocation + "&api-version=" + apiVersion + "&sdk-version=" + sdkVersion)
					.setHeaders(headers);
			try {
				OctaneResponse octaneResponse = restClient.execute(octaneRequest);
				if (octaneResponse.getStatus() == HttpStatus.SC_OK) {
					responseBody = octaneResponse.getBody();
				} else {
					if (octaneResponse.getStatus() == HttpStatus.SC_REQUEST_TIMEOUT) {
						logger.info("expected timeout disconnection on retrieval of abridged tasks");
					} else if (octaneResponse.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
						logger.error("connection to Octane Server failed: authentication error");
						doBreakableWait(5000);
					} else if (octaneResponse.getStatus() == HttpStatus.SC_FORBIDDEN) {
						logger.error("connection to Octane Server failed: authorization error");
						doBreakableWait(5000);
					} else if (octaneResponse.getStatus() == HttpStatus.SC_NOT_FOUND) {
						logger.error("connection to Octane Server failed: 404, API changes? version problem?");
						doBreakableWait(20000);
					} else {
						logger.info("unexpected response; status: " + octaneResponse.getStatus() + "; content: " + octaneResponse.getBody());
						doBreakableWait(2000);
					}
				}
			} catch (Exception e) {
				logger.error("failed to retrieve abridged tasks", e);
				doBreakableWait(2000);
			}
			return responseBody;
		} else {
			logger.info("Octane is not configured on this plugin, breathing before next retry");
			doBreakableWait(5000);
			return null;
		}
	}

	public void shutdown() {
		connectivityExecutors.shutdownNow();
	}

	private void handleTasks(String tasksJSON) {
		try {
			OctaneTaskAbridged[] tasks = dtoFactory.dtoCollectionFromJson(tasksJSON, OctaneTaskAbridged[].class);
			logger.info("going to process " + tasks.length + " tasks");
			for (final OctaneTaskAbridged task : tasks) {
				taskProcessingExecutors.execute(new Runnable() {
					public void run() {
						OctaneResultAbridged result = tasksProcessor.execute(task);
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
		RestClient restClientImpl = restService.obtainClient();
		OctaneConfiguration octaneConfiguration = pluginServices.getOctaneConfiguration();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/json");
		OctaneRequest octaneRequest = dtoFactory.newDTO(OctaneRequest.class)
				.setMethod(HttpMethod.PUT)
				.setUrl(octaneConfiguration.getUrl() + "/internal-api/shared_spaces/" + octaneConfiguration.getSharedSpace() + "/analytics/ci/servers/" + selfIdentity + "/tasks/" + taskId + "/result")
				.setHeaders(headers)
				.setBody(contentJSON);
		try {
			OctaneResponse octaneResponse = restClientImpl.execute(octaneRequest);
			return octaneResponse.getStatus();
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
