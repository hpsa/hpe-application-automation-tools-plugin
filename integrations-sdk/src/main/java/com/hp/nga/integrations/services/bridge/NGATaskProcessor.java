package com.hp.nga.integrations.services.bridge;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.nga.integrations.services.SDKFactory;

import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by gullery on 17/08/2015.
 * <p>
 * Tasks Processor handles NGA tasks, both coming from abridged logic as well as plugin's REST call delegation
 */

public class NGATaskProcessor {
	private static final Logger logger = Logger.getLogger(NGATaskProcessor.class.getName());
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String NGA = "nga";
	private static final String STATUS = "status";
	private static final String JOBS = "jobs";
	private static final String RUN = "run";
	private static final String HISTORY = "history";
	private static final String BUILDS = "builds";
	private static final String LATEST = "lastBuild";

	private final NGATaskAbridged task;

	public NGATaskProcessor(NGATaskAbridged task) {
		if (task == null) {
			throw new IllegalArgumentException("task MUST NOT be null");
		}
		if (task.getUrl() == null || task.getUrl().isEmpty()) {
			throw new IllegalArgumentException("task 'URL' MUST NOT be null nor empty");
		}
		if (!task.getUrl().contains(NGA)) {
			throw new IllegalArgumentException("task 'URL' expected to contain '" + NGA + "'; wrong handler call?");
		}

		this.task = task;
	}

	public NGAResultAbridged execute() {
		logger.info("BRIDGE: processing task '" + task.getId() + "': " + task.getMethod() + " " + task.getUrl());

		NGAResultAbridged result = DTOFactory.getInstance().newDTO(NGAResultAbridged.class);
		result.setId(task.getId());
		result.setStatus(200);
		String[] path = Pattern.compile("^.*" + NGA + "/?").matcher(task.getUrl()).replaceFirst("").split("/");
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
					result.setStatus(501);
				}
			} else if (path.length == 3 && JOBS.equals(path[0]) && HISTORY.equals(path[2])) {
				executeHistoryRequest(result, path[1], task.getBody());
			} else {
				result.setStatus(404);
			}
		} catch (Exception e) {
			logger.warning("can't execute the task\n" + e.getStackTrace());
			result.setStatus(500);
		}

		logger.info("BRIDGE: result for task '" + task.getId() + "' available with status " + result.getStatus());
		return result;
	}

	private void executeStatusRequest(NGAResultAbridged result) {
		CIPluginServices dataProvider = SDKFactory.getCIPluginServices();
		CIProviderSummaryInfo status = dtoFactory.newDTO(CIProviderSummaryInfo.class)
				.setServer(dataProvider.getServerInfo())
				.setPlugin(dataProvider.getPluginInfo());
		result.setBody(dtoFactory.dtoToJson(status));
	}

	private void executeJobsListRequest(NGAResultAbridged result, boolean includingParameters) {
		CIJobsList content = SDKFactory.getCIPluginServices().getJobsList(includingParameters);
		result.setBody(dtoFactory.dtoToJson(content));
	}

	private void executePipelineRequest(NGAResultAbridged result, String jobId) {
		PipelineNode content = SDKFactory.getCIPluginServices().getPipeline(jobId);
		result.setBody(dtoFactory.dtoToJson(content));
	}

	private void executePipelineRunRequest(NGAResultAbridged result, String jobId, String originalBody) {
		int status = SDKFactory.getCIPluginServices().runPipeline(jobId, originalBody);
		result.setStatus(status);
	}

	private void executeLatestSnapshotRequest(NGAResultAbridged result, String jobId, boolean subTree) {
		SnapshotNode content = SDKFactory.getCIPluginServices().getSnapshotLatest(jobId, subTree);
		result.setBody(dtoFactory.dtoToJson(content));
	}

	private void executeHistoryRequest(NGAResultAbridged result, String jobId, String originalBody) {
		BuildHistory content = SDKFactory.getCIPluginServices().getHistoryPipeline(jobId, originalBody);
		result.setBody(dtoFactory.dtoToJson(content));
	}
}
