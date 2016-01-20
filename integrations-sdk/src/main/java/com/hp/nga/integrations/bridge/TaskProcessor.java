package com.hp.nga.integrations.bridge;

import com.hp.nga.integrations.configuration.ServerConfiguration;

import java.util.logging.Logger;

/**
 * Created by gullery on 17/08/2015.
 * <p/>
 * This class is a Tasks Processor for abridged connectivity and to be run in separate thread
 */

public class TaskProcessor implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskProcessor.class.getName());
	private final ServerConfiguration serverConfig;
	private final AbridgedTask task;

	TaskProcessor(AbridgedTask task, ServerConfiguration serverConfig) {
		this.task = task;
		this.serverConfig = serverConfig;
	}

	public void run() {
		logger.info("BRIDGE: processing task '" + task.getId() + "': " + task.getMethod() + " " + task.getUrl());

		//  TODO: this one should be replaced by router's logic
//		LoopBackRestService.LoopBackResponse response;
//		try {
//			if ("GET".equals(task.getMethod())) {
//				response = LoopBackRestService.loopBackGet(task.getUrl(), task.getHeaders());
//			} else if ("PUT".equals(task.getMethod())) {
//				response = LoopBackRestService.loopBackPut(task.getUrl(), task.getHeaders(), task.getBody());
//			} else if ("POST".equals(task.getMethod())) {
//				response = LoopBackRestService.loopBackPost(task.getUrl(), task.getHeaders(), task.getBody());
//			} else {
//				response = new LoopBackRestService.LoopBackResponse(415, null, "");
//			}
//		} catch (Exception e) {
//			logger.severe("BRIDGE: failed to process task '" + task.getId() + "', returning 500:" + e.getMessage());
//			response = new LoopBackRestService.LoopBackResponse(500, null, e.getMessage());
//		}

//		MqmRestClient restClient = clientFactory.create(serverConfig.getUrl(), serverConfig.getSharedSpace(), serverConfig.getUsername(), serverConfig.getPassword());
		AbridgedResult result = new AbridgedResult();
		result.setId(task.getId());
//		result.setStatus(response.statusCode);
//		result.setHeaders(response.headers);
//		result.setBody(response.body);
//
		int submitStatus = 200;
		logger.info("BRIDGE: result for task '" + task.getId() + "' submitted with status " + submitStatus);
	}
}
