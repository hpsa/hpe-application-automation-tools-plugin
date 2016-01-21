package com.hp.nga.integrations.bridge;

import com.hp.nga.integrations.api.CIDataProvider;
import com.hp.nga.integrations.dto.general.AggregatedStatusInfo;
import com.hp.nga.integrations.dto.rest.AbridgedResult;
import com.hp.nga.integrations.dto.rest.AbridgedTask;
import com.hp.nga.integrations.serialization.SerializationService;

import java.util.logging.Logger;

/**
 * Created by gullery on 17/08/2015.
 * <p>
 * Tasks Processor handles NGA tasks, both coming from abridged logic as well as plugin's REST call delegation
 */

public class TaskProcessor {
	private static final Logger logger = Logger.getLogger(TaskProcessor.class.getName());
	private final AbridgedTask task;

	public TaskProcessor(AbridgedTask task) {
		if (task == null) {
			throw new IllegalArgumentException("task MUST NOT be null");
		}

		this.task = task;
	}

	public AbridgedResult execute() {
		logger.info("BRIDGE: processing task '" + task.getId() + "': " + task.getMethod() + " " + task.getUrl());

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

		logger.info("BRIDGE: result for task '" + task.getId() + "' available with status " + result.getStatus());
		return result;
	}
}
