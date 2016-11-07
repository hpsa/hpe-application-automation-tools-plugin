package com.hp.octane.integrations.services.tasking;

import com.hp.octane.integrations.spi.CIPluginServices;
import com.hp.octane.integrations.api.TasksProcessor;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.connectivity.OctaneTaskAbridged;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIPluginSDKInfo;
import com.hp.octane.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.octane.integrations.dto.pipelines.BuildHistory;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.integrations.exceptions.ConfigurationException;
import com.hp.octane.integrations.exceptions.PermissionException;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by gullery on 17/08/2015.
 * <p/>
 * Tasks routing service handles NGA tasks, both coming from abridged logic as well as plugin's REST call delegation
 */

public final class TasksProcessorImpl extends OctaneSDK.SDKServiceBase implements TasksProcessor {
	private static final Logger logger = LogManager.getLogger(TasksProcessorImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String NGA_API = "nga/api/v1";
	private static final String STATUS = "status";
	private static final String JOBS = "jobs";
	private static final String RUN = "run";
	private static final String HISTORY = "history";
	private static final String BUILDS = "builds";
	private static final String LATEST = "latest";

	private final CIPluginServices pluginServices;

	public TasksProcessorImpl(Object configurator, CIPluginServices pluginServices) {
		super(configurator);

		if (pluginServices == null) {
			throw new IllegalArgumentException("plugin services MUST NOT be null");
		}

		this.pluginServices = pluginServices;
	}

	public OctaneResultAbridged execute(OctaneTaskAbridged task) {
		if (task == null) {
			throw new IllegalArgumentException("task MUST NOT be null");
		}
		if (task.getUrl() == null || task.getUrl().isEmpty()) {
			throw new IllegalArgumentException("task 'URL' MUST NOT be null nor empty");
		}
		if (!task.getUrl().contains(NGA_API)) {
			throw new IllegalArgumentException("task 'URL' expected to contain '" + NGA_API + "'; wrong handler call?");
		}
		logger.info("processing task '" + task.getId() + "': " + task.getMethod() + " " + task.getUrl());

		OctaneResultAbridged result = DTOFactory.getInstance().newDTO(OctaneResultAbridged.class);
		result.setId(task.getId());
		result.setStatus(200);
		result.setHeaders(new HashMap<String, String>());
		String[] path = Pattern.compile("^.*" + NGA_API + "/?").matcher(task.getUrl()).replaceFirst("").split("/");
		try {
			if (path.length == 1 && STATUS.equals(path[0])) {
				executeStatusRequest(result);
			} else if (path.length == 1 && path[0].startsWith(JOBS)) {
				executeJobsListRequest(result, !path[0].contains("parameters=false"));
			} else if (path.length == 2 && JOBS.equals(path[0])) {
				executePipelineRequest(result, path[1]);
			} else if (path.length == 3 && JOBS.equals(path[0]) && RUN.equals(path[2])) {
				executePipelineRunRequest(result, path[1], task.getBody());
			} else if (path.length == 4 && JOBS.equals(path[0]) && BUILDS.equals(path[2])) {
				//TODO: in the future should take the last parameter from the request
				boolean subTree = false;
				if (LATEST.equals(path[3])) {
					executeLatestSnapshotRequest(result, path[1], subTree);
				} else {
					executeSnapshotByNumberRequest(result, path[1], path[3], subTree);
				}
			} else if (path.length == 3 && JOBS.equals(path[0]) && HISTORY.equals(path[2])) {
				executeHistoryRequest(result, path[1], task.getBody());
			} else {
				result.setStatus(404);
			}
		} catch (PermissionException pe) {
			logger.warn("task execution failed; error: " + pe.getErrorCode());
			result.setStatus(pe.getErrorCode());
			result.setBody(String.valueOf(pe.getErrorCode()));
		} catch (ConfigurationException ce) {
			logger.warn("task execution failed; error: " + ce.getErrorCode());
			result.setStatus(404);
			result.setBody(String.valueOf(ce.getErrorCode()));
		} catch (Exception e) {
			logger.error("task execution failed", e);
			result.setStatus(500);
		}

		logger.info("result for task '" + task.getId() + "' available with status " + result.getStatus());
		return result;
	}

	private void executeStatusRequest(OctaneResultAbridged result) {
		CIPluginSDKInfo sdkInfo = dtoFactory.newDTO(CIPluginSDKInfo.class)
				.setApiVersion(OctaneSDK.API_VERSION)
				.setSdkVersion(OctaneSDK.SDK_VERSION);
		CIProviderSummaryInfo status = dtoFactory.newDTO(CIProviderSummaryInfo.class)
				.setServer(pluginServices.getServerInfo())
				.setPlugin(pluginServices.getPluginInfo())
				.setSdk(sdkInfo);
		result.setBody(dtoFactory.dtoToJson(status));
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executeJobsListRequest(OctaneResultAbridged result, boolean includingParameters) {
		CIJobsList content = pluginServices.getJobsList(includingParameters);
		result.setBody(dtoFactory.dtoToJson(content));
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executePipelineRequest(OctaneResultAbridged result, String jobId) {
		PipelineNode content = pluginServices.getPipeline(jobId);
		result.setBody(dtoFactory.dtoToJson(content));
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executePipelineRunRequest(OctaneResultAbridged result, String jobId, String originalBody) {
		pluginServices.runPipeline(jobId, originalBody);
		result.setStatus(201);
	}

	private void executeLatestSnapshotRequest(OctaneResultAbridged result, String jobId, boolean subTree) {
		SnapshotNode data = pluginServices.getSnapshotLatest(jobId, subTree);
		if (data != null) {
			result.setBody(dtoFactory.dtoToJson(data));
		} else {
			result.setStatus(404);
		}
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executeSnapshotByNumberRequest(OctaneResultAbridged result, String jobCiId, String buildCiId, boolean subTree) {
		SnapshotNode data = pluginServices.getSnapshotByNumber(jobCiId, buildCiId, subTree);
		if (data != null) {
			result.setBody(dtoFactory.dtoToJson(data));
		} else {
			result.setStatus(404);
		}
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executeHistoryRequest(OctaneResultAbridged result, String jobId, String originalBody) {
		BuildHistory content = pluginServices.getHistoryPipeline(jobId, originalBody);
		result.setBody(dtoFactory.dtoToJson(content));
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}
}
