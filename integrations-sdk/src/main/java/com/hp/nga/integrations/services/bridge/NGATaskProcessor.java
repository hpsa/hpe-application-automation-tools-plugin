package com.hp.nga.integrations.services.bridge;

import com.hp.nga.integrations.NGAPluginSDK;
import com.hp.nga.integrations.api.CIPluginService;
import com.hp.nga.integrations.dto.general.AggregatedStatusInfo;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.projects.JobsListDTO;
import com.hp.nga.integrations.dto.rest.NGAResult;
import com.hp.nga.integrations.dto.rest.NGATask;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.nga.integrations.services.serialization.SerializationService;

import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by gullery on 17/08/2015.
 * <p>
 * Tasks Processor handles NGA tasks, both coming from abridged logic as well as plugin's REST call delegation
 */

public class NGATaskProcessor {
	private static final Logger logger = Logger.getLogger(NGATaskProcessor.class.getName());
	private static final String OCTANE = "octane";
	private static final String STATUS = "status";
	private static final String PROJECTS = "projects";
	private static final String RUN = "run";
	private static final String HISTORY = "history";
	private static final String BUILDS = "builds";
	private static final String LATEST = "latest";

	private final NGATask task;

	public NGATaskProcessor(NGATask task) {
		if (task == null) {
			throw new IllegalArgumentException("task MUST NOT be null");
		}
		if (task.getUrl() == null || task.getUrl().isEmpty()) {
			throw new IllegalArgumentException("task 'URL' MUST NOT be null nor empty");
		}
		if (!task.getUrl().contains(OCTANE)) {
			throw new IllegalArgumentException("task 'URL' expected to contain '" + OCTANE + "'; wrong handler call?");
		}

		this.task = task;
	}

	public NGAResult execute() {
		logger.info("BRIDGE: processing task '" + task.getId() + "': " + task.getMethod() + " " + task.getUrl());

		NGAResult result = new NGAResult();
		result.setId(task.getId());
		result.setStatus(200);
		String[] path = Pattern.compile("^.*" + OCTANE + "/?").matcher(task.getUrl()).replaceFirst("").split("/");
		try {
			if (path.length == 1 && STATUS.equals(path[0])) {
				executeStatusRequest(result);
			} else if (path.length == 1 && path[0].startsWith(PROJECTS)) {
				executeProjectsListRequest(result, !path[0].contains("parameters=false"));
			} else if (path.length == 2 && PROJECTS.equals(path[0])) {
				executePipelineRequest(result, path[1]);
			} else if (path.length == 3 && PROJECTS.equals(path[0]) && RUN.equals(path[2])) {
				executeProjectRunRequest(result, path[1]);
			} else if (path.length == 4 && PROJECTS.equals(path[0]) && BUILDS.equals(path[2])) {
				//TODO: in the future should take the last paramter from the request
				executeSnapshotRequest(result, path[1], path[3],false);
			} else if (path.length == 3 && PROJECTS.equals(path[0]) && HISTORY.equals(path[2])) {
				executeHistoryRequest(result);
			} else {
				result.setStatus(404);
			}
		} catch (Exception e) {
			result.setStatus(500);
		}

		logger.info("BRIDGE: result for task '" + task.getId() + "' available with status " + result.getStatus());
		return result;
	}

	private void executeStatusRequest(NGAResult result) {
		CIPluginService dataProvider = NGAPluginSDK.getInstance().getCiPluginService();
		AggregatedStatusInfo status = new AggregatedStatusInfo();
		status.setServer(dataProvider.getServerInfo());
		status.setPlugin(dataProvider.getPluginInfo());
		result.setBody(SerializationService.toJSON(status));
	}

	private void executeProjectsListRequest(NGAResult result, boolean includingParameters) {
		JobsListDTO content = NGAPluginSDK.getInstance().getCiPluginService().getProjectsList(includingParameters);
		result.setBody(SerializationService.toJSON(content));
	}

	private void executePipelineRequest(NGAResult result, String projectId) {
		StructureItem content = NGAPluginSDK.getInstance().getCiPluginService().getPipeline(projectId);
		result.setBody(SerializationService.toJSON(content));
	}

	private void executeProjectRunRequest(NGAResult result, String projectId) {
		//  TODO: here need to get body of the request as well
	}

	private void executeSnapshotRequest(NGAResult result, String projectId, String buildId,boolean subTree) {
		SnapshotItem content = NGAPluginSDK.getInstance().getCiPluginService().getSnapshotLatest(projectId, buildId, false);
		result.setBody(SerializationService.toJSON(content));
	}

	private void executeHistoryRequest(NGAResult result) {

	}
}
