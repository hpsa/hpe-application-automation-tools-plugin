package com.hp.octane.plugins.jenkins.bridge;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by gullery on 17/08/2015.
 * <p/>
 * This class is a Tasks Processor for abridged connectivity and to be run in separate thread
 */

public class TaskProcessor implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskProcessor.class.getName());
	private final JSONObject task;
	private final String baseURL;

	TaskProcessor(JSONObject task, String baseURL) {
		this.task = task;
		this.baseURL = baseURL;
	}

	@Override
	public void run() {
		String id = task.getString("id");
		String method = task.getString("method");
		String url = task.getString("url");
		Map<String, String> headers = task.containsKey("headers") ? buildHeadersMap(task.getJSONObject("headers")) : null;
		logger.info("BRIDGE: processing task '" + id + "': " + method + " " + url);

		RESTClientTMP.LoopbackResponse response = null;
		if (method.equals("GET")) {
			response = RESTClientTMP.loopbackGet(url, headers);
		} else if (method.equals("PUT")) {
			response = RESTClientTMP.loopbackPut(url, headers, task.getString("body"));
		} else if (method.equals("POST")) {
			response = RESTClientTMP.loopbackPost(url, headers, task.getString("body"));
		}
		RESTClientTMP.putTaskResult(baseURL + "/" + id + "/result", response);
	}

	private Map<String, String> buildHeadersMap(JSONObject json) {
		Map<String, String> result = new HashMap<String, String>();
		for (Object key : json.keySet()) result.put((String) key, json.getString((String) key));
		return result;
	}
}
