package com.hp.octane.plugins.common.bridge;

import com.hp.mqm.client.MqmRestClient;
import com.hp.octane.plugins.common.bridge.tasks.CITaskService;
import com.hp.octane.plugins.common.configuration.ServerConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.client.MqmRestClientFactory;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;
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
	private final String ciType;
	private final ServerConfiguration mqmConfiguration;
	private final CITaskService ciTaskService;

	TaskProcessor(JSONObject task,String ciType, ServerConfiguration mqmConfiguration, CITaskService ciTaskService) {
		this.task = task;
		this.ciType = ciType;
		this.mqmConfiguration = mqmConfiguration;
		this.ciTaskService = ciTaskService;
	}

	@Override
	public void run() {
		String id = task.getString("id");
		String method = task.getString("method");
		String url = task.getString("url");
		Map<String, String> headers = task.containsKey("headers") ? buildHeadersMap(task.getJSONObject("headers")) : null;

		logger.info("BRIDGE: processing task '" + id + "': " + method + " " + url);
		String response = "Unknown Command";
		//TODO:Add URL Routing
		if(url.contains("status")){
			response = ciTaskService.getStatus();
		}else if(url.contains("jobs")){
			response = ciTaskService.getProjects(false);
		}else if(url.contains("/structure")){
			//String ciId = url.substring(/([^/]+)(?=/[^/]+/?$)/); url.indexOf("/job");
			String ciId = url.substring(url.indexOf("/job")+5, url.indexOf("/octane"));
			logger.info("BRIDGE: processing structure '" + ciId);
			response = ciTaskService.getStructure(ciId);
		}else if(url.contains("/snapshot")){
			String ciId = url.substring(url.indexOf("/job")+5, url.indexOf("/lastBuild"));
			logger.info("BRIDGE: processing live '" + ciId);
			response = ciTaskService.getSnapshot("Test_Ayellet"/*ciId*/);
		}
		MqmRestClient restClient = MqmRestClientFactory.create(
				ciType,
				mqmConfiguration.location,
				mqmConfiguration.sharedSpace,
				mqmConfiguration.username,
				mqmConfiguration.password);
		JSONObject json = new JSONObject();
		json.put("statusCode", 200);
		json.put("headers", new HashMap<String, String>());
		//json.put("body", response.body);
		json.put("body", response);


		NGAPlugin ngaPlugin = NGAPlugin.getInstance();
		Config cfg = ngaPlugin.getConfig();


		int submitStatus = restClient.putAbridgedResult(
				cfg.getIdentity()/*new PluginActions.ServerInfo().getInstanceId()*/,
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
