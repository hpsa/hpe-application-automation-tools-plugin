package com.hp.octane.plugins.common.bridge;

import com.hp.mqm.client.MqmRestClient;
import com.hp.octane.plugins.common.configuration.ServerConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.client.TeamCityMqmRestClientFactory;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by gullery on 17/08/2015.
 * <p/>
 * This class is a Tasks Processor for abridged connectivity and to be run in separate thread
 */

public class TaskProcessor implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskProcessor.class.getName());
	private final JSONObject task;
	private final TeamCityMqmRestClientFactory clientFactory;
	private final ServerConfiguration mqmConfiguration;

	TaskProcessor(JSONObject task, TeamCityMqmRestClientFactory clientFactory, ServerConfiguration mqmConfiguration) {
		this.task = task;
		this.clientFactory = clientFactory;
		this.mqmConfiguration = mqmConfiguration;
	}

	@Override
	public void run() {
		String id = task.getString("id");
		String method = task.getString("method");
		String url = task.getString("url");
		Map<String, String> headers = task.containsKey("headers") ? buildHeadersMap(task.getJSONObject("headers")) : null;
		String body;
		logger.info("BRIDGE: processing task '" + id + "': " + method + " " + url);

		LoopBackRestService.LoopBackResponse response;
		try {
			if (method.equals("GET")) {
				response = LoopBackRestService.loopBackGet(url, headers);
			} else if (method.equals("PUT")) {
				body = obtainBody();
				response = LoopBackRestService.loopBackPut(url, headers, body);
			} else if (method.equals("POST")) {
				body = obtainBody();
				response = LoopBackRestService.loopBackPost(url, headers, body);
			} else {
				response = new LoopBackRestService.LoopBackResponse(415, null, "");
			}
		} catch (Exception e) {
			logger.severe("BRIDGE: failed to process task '" + id + "', returning 500:" + e.getMessage());
			response = new LoopBackRestService.LoopBackResponse(500, null, e.getMessage());
		}

		MqmRestClient restClient = clientFactory.create(
				mqmConfiguration.location,
				mqmConfiguration.sharedSpace,
				mqmConfiguration.username,
				mqmConfiguration.password);
		JSONObject json = new JSONObject();
		json.put("statusCode", response.statusCode);
		json.put("headers", response.headers);
		json.put("body", response.body);

		int submitStatus = restClient.putAbridgedResult(
				"uuid"/*new PluginActions.ServerInfo().getInstanceId()*/,
				id,
				json.toString());
		logger.info("BRIDGE: result for task '" + id + "' submitted with status " + submitStatus);
	}

	private Map<String, String> buildHeadersMap(JSONObject json) {
		Map<String, String> result = new HashMap<String, String>();
		for (Object key : json.keySet()) result.put((String) key, json.getString((String) key));
		return result;
	}

	private String obtainBody() {
		String result;
		if (task.containsKey("body") && task.get("body") != null) {
			result = task.getString("body");
		} else {
			result = "";
		}
		return result;
	}
}
