package com.hp.octane.plugins.jenkins.bridge;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by gullery on 17/08/2015.
 * <p/>
 * This class is a Tasks Processor definition to be run in separate thread
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
		if (task.getString("method").toLowerCase().equals("get")) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("accept", "application/json");
			String resultBody = RESTClientTMP.get(task.getString("url"), headers);
			RESTClientTMP.put(baseURL + "/" + task.getString("id") + "/result", 200, null, resultBody);
		}
	}
}
