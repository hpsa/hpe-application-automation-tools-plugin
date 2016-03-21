package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.api.TasksProcessor;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.general.CIPluginSDKInfo;
import com.hp.nga.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.nga.integrations.exceptions.ConfigurationException;
import com.hp.nga.integrations.exceptions.PermissionException;
import com.hp.nga.integrations.SDKManager;
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

final class TasksProcessorImpl implements TasksProcessor {
	private static final Logger logger = LogManager.getLogger(TasksProcessorImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String NGA_API = "nga/api/v1";
	private static final String STATUS = "status";
	private static final String JOBS = "jobs";
	private static final String RUN = "run";
	private static final String HISTORY = "history";
	private static final String BUILDS = "builds";
	private static final String LATEST = "latest";

	private final SDKManager manager;

	TasksProcessorImpl(SDKManager manager) {
		this.manager = manager;
	}

	public NGAResultAbridged execute(NGATaskAbridged task) {
		if (task == null) {
			throw new IllegalArgumentException("task MUST NOT be null");
		}
		if (task.getUrl() == null || task.getUrl().isEmpty()) {
			throw new IllegalArgumentException("task 'URL' MUST NOT be null nor empty");
		}
		if (!task.getUrl().contains(NGA_API)) {
			throw new IllegalArgumentException("task 'URL' expected to contain '" + NGA_API + "'; wrong handler call?");
		}
		logger.info("TasksRouter: processing task '" + task.getId() + "': " + task.getMethod() + " " + task.getUrl());

		NGAResultAbridged result = DTOFactory.getInstance().newDTO(NGAResultAbridged.class);
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
		} catch (PermissionException jenkinsRequestException) {
			logger.warn("TasksRouter: task execution failed; error: " + jenkinsRequestException.getErrorCode());
			result.setStatus(jenkinsRequestException.getErrorCode());
			result.setBody(String.valueOf(jenkinsRequestException.getErrorCode()));
		} catch (ConfigurationException ce) {
			logger.warn("TasksRouter: task execution failed; error: " + ce.getErrorCode());
			result.setStatus(404);
			result.setBody(String.valueOf(ce.getErrorCode()));
		} catch (Exception e) {
			logger.error("TasksRouter: task execution failed", e);
			result.setStatus(500);
		}

		logger.info("TasksRouter: result for task '" + task.getId() + "' available with status " + result.getStatus());
		return result;
	}

	private void executeStatusRequest(NGAResultAbridged result) {
		CIPluginServices dataProvider = manager.getCIPluginServices();
		CIPluginSDKInfo sdkInfo = dtoFactory.newDTO(CIPluginSDKInfo.class)
				.setApiVersion(manager.API_VERSION)
				.setSdkVersion(manager.SDK_VERSION);
		CIProviderSummaryInfo status = dtoFactory.newDTO(CIProviderSummaryInfo.class)
				.setServer(dataProvider.getServerInfo())
				.setPlugin(dataProvider.getPluginInfo())
				.setSdk(sdkInfo);
		result.setBody(dtoFactory.dtoToJson(status));
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executeJobsListRequest(NGAResultAbridged result, boolean includingParameters) {
		CIJobsList content = manager.getCIPluginServices().getJobsList(includingParameters);
		result.setBody(dtoFactory.dtoToJson(content));
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executePipelineRequest(NGAResultAbridged result, String jobId) {
		PipelineNode content = manager.getCIPluginServices().getPipeline(jobId);
		result.setBody(dtoFactory.dtoToJson(content));
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executePipelineRunRequest(NGAResultAbridged result, String jobId, String originalBody) {
		manager.getCIPluginServices().runPipeline(jobId, originalBody);
		result.setStatus(201);
	}

	private void executeLatestSnapshotRequest(NGAResultAbridged result, String jobId, boolean subTree) {
		SnapshotNode data = manager.getCIPluginServices().getSnapshotLatest(jobId, subTree);
		if (data != null) {
			result.setBody(dtoFactory.dtoToJson(data));
		} else {
			result.setStatus(404);
		}
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executeSnapshotByNumberRequest(NGAResultAbridged result, String jobCiId, String buildCiId, boolean subTree) {
		SnapshotNode content = manager.getCIPluginServices().getSnapshotByNumber(jobCiId, buildCiId, subTree);
		result.setBody(dtoFactory.dtoToJson(content));
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	private void executeHistoryRequest(NGAResultAbridged result, String jobId, String originalBody) {
		BuildHistory content = manager.getCIPluginServices().getHistoryPipeline(jobId, originalBody);
		result.setBody(dtoFactory.dtoToJson(content));
		result.getHeaders().put(HttpHeaders.CONTENT_TYPE, "application/json");
	}
}
