package com.hp.nga.integrations.bridge;

import com.hp.nga.integrations.api.CIDataProvider;
import com.hp.nga.integrations.dto.general.AggregatedStatusInfo;
import com.hp.nga.integrations.dto.rest.AbridgedResult;
import com.hp.nga.integrations.dto.rest.AbridgedTask;

import java.util.logging.Logger;

/**
 * Created by gullery on 17/08/2015.
 * <p>
 * Tasks Processor serves NGA (abridged) connectivity
 */

public class TaskProcessor implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskProcessor.class.getName());
	private final AbridgedTask task;

	public TaskProcessor(AbridgedTask task) {
		this.task = task;
	}

	public void run() {
		logger.info("BRIDGE: processing task '" + task.getId() + "': " + task.getMethod() + " " + task.getUrl());
		CIDataProvider ciDataProvider = CIDataProvider.getInstance();
		AbridgedResult result = new AbridgedResult();
		result.setId(task.getId());

		try {
			if (task.getMethod().equals("status")) {
				AggregatedStatusInfo status = new AggregatedStatusInfo();
				status.setServer(ciDataProvider.getServerInfo());
				status.setPlugin(ciDataProvider.getPluginInfo());
			}
			result.setStatus(200);
		} catch (Exception e) {
			result.setStatus(500);
		}

//		int submitStatus = restClient.putAbridgedResult(
//				ciDataProvider.getServerInfo().getInstanceId(),
//				task.getId(),
//				result.getBody());
//
//		logger.info("BRIDGE: result for task '" + task.getId() + "' submitted with status " + submitStatus);
	}
}
