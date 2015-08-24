package com.hp.octane.plugins.jenkins.bridge;

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
	private final String baseURL;

	TaskProcessor(String taskJSON, String baseURL) {
		task = JSONObject.fromObject(taskJSON);
		this.baseURL = baseURL;
	}

	@Override
	public void run() {
		String url = task.getString("url");
		Map<String, String> headers = task.containsKey("headers") ? buildHeadersMap(task.getJSONObject("headers")) : null;
		String resultBody;
		if (task.getString("method").equals("GET")) {
			resultBody = RESTClientTMP.get(url, headers);
		} else if (task.getString("method").equals("PUT")) {
			//  add suppord for PUT method
			resultBody = "not available";
		} else if (task.getString("method").equals("POST")) {
			//  add support for POST method
			resultBody = "not available";
		} else {
			resultBody = "not supported method";
		}
		RESTClientTMP.put(baseURL + "/" + task.getString("id") + "/result", 200, null, resultBody);
	}

	private Map<String, String> buildHeadersMap(JSONObject json) {
		Map<String, String> result = new HashMap<String, String>();
		result.put("accept", "application/json");
		return result;
	}
}
