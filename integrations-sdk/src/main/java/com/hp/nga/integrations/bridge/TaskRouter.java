package com.hp.nga.integrations.bridge;

import com.hp.nga.integrations.api.CIDataProvider;
import com.hp.nga.integrations.dto.general.AggregatedStatusInfo;
import com.hp.nga.integrations.dto.rest.AbridgedResult;
import com.hp.nga.integrations.dto.rest.AbridgedTask;
import com.hp.nga.integrations.serialization.SerializationService;

/**
 * Created by gullery on 20/01/2016.
 * <p>
 * TO BE REMOVED
 */

public class TaskRouter {
	private final AbridgedTask task;

	public TaskRouter(AbridgedTask task) {
		if (task == null) {
			throw new IllegalArgumentException("task MUST NOT be null");
		}

		this.task = task;
	}

	// 		GenericHttpResponse response = executeRequest(ciServer, HttpMethod.GET, "/octane/status", null, null);
	//		GenericHttpResponse response = executeRequest(ciServer, HttpMethod.GET, "/octane/jobs?parameters=" + includeParameters, null, null);
	//	    GenericHttpResponse r = executeRequest(ciServer, HttpMethod.GET, "/octane/projects/" + ciJobName, null, null);
	//			GenericHttpResponse r = executeRequest(ciServer, HttpMethod.GET, "/octane/projects/" + ciProjectRefId + "/builds/" + ciBuildRefId, null, null);
	//					"/octane/projects/" + ciProjectRefId + "/run",
	//			GenericHttpResponse response = executeRequest(ciServer, HttpMethod.GET, "/job/" + ciProjectRefId + "/octane/history", null, null);

	public AbridgedResult calculateResult() {
		CIDataProvider dataProvider = CIDataProvider.getInstance();
		AbridgedResult result = new AbridgedResult();
		result.setId(task.getId());
		try {
			if (task.getUrl().endsWith("status")) {
				AggregatedStatusInfo status = new AggregatedStatusInfo();
				status.setServer(dataProvider.getServerInfo());
				status.setPlugin(dataProvider.getPluginInfo());
				result.setStatus(200);
				result.setBody(SerializationService.toJSON(status));
			} else if (task.getUrl().contains("projects?parameters")) {
				//  TODO: get jobs list
			} else if (task.getUrl().contains("projects/")) {
				//  TODO: get job structure
			} else if (task.getUrl().contains("/builds/")) {
				//  TODO: get build snapshot
			} else if (task.getUrl().contains("run")) {
				//  TODO: execute run
			} else if (task.getUrl().contains("history")) {
				//  TODO: do Almog's history API
			} else {
				result.setStatus(404);
			}
		} catch (Exception e) {
			result.setStatus(500);
		}

		return result;
	}
}
