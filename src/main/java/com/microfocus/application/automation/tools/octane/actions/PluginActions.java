/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.octane.actions;

import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.connectivity.HttpMethod;
import com.hp.octane.integrations.dto.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.connectivity.OctaneTaskAbridged;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.hp.octane.integrations.services.tasking.TasksProcessor;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.microfocus.application.automation.tools.octane.configuration.ConfigApi;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import hudson.Extension;
import hudson.model.RootAction;
import net.sf.json.JSONObject;
import org.apache.http.entity.ContentType;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 8/10/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class PluginActions implements RootAction {
	private String STATUS_REQUEST = "/nga/api/v1/status";

	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "nga";
	}

	public ConfigApi getConfiguration() {
		return new ConfigApi();
	}

	public void doDynamic(StaplerRequest req, StaplerResponse res) throws IOException {

		if (req.getRequestURI().toLowerCase().contains(STATUS_REQUEST)) {
			JSONObject result = getStatusResult();
			res.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
			res.setStatus(200);
			res.getWriter().write(result.toString());
			return;
		} else {
			res.setStatus(404);
			res.getWriter().write("");
			return;
		}
	}

	private JSONObject getStatusResult() {
		JSONObject sdkJson = new JSONObject();
		sdkJson.put("sdkVersion", OctaneSDK.SDK_VERSION);
		JSONObject pluginJson = new JSONObject();
		pluginJson.put("version", ConfigurationService.getPluginVersion());
		JSONObject serverInfoJson = new JSONObject();
		CIServerInfo serverInfo = CIJenkinsServicesImpl.getJenkinsServerInfo();
		serverInfoJson.put("type", serverInfo.getType());
		serverInfoJson.put("version", serverInfo.getVersion());
		serverInfoJson.put("url", serverInfo.getUrl());
		/*JSONArray configurationsJson = new JSONArray();
		for (OctaneServerSettingsModel settings : ConfigurationService.getAllSettings()) {
			if (settings.isValid()) {
				JSONObject configJson = new JSONObject();
				configJson.put("identity", settings.getIdentity());
				configJson.put("location", settings.getLocation());
				configJson.put("sharedSpace", settings.getSharedSpace());
				//configJson.put("username", settings.getUsername());
				//configJson.put("impersonatedUser", settings.getImpersonatedUser());
				configurationsJson.add(configJson);
			}
		}*/

		JSONObject result = new JSONObject();
		result.put("sdk", sdkJson);
		result.put("plugin", pluginJson);
		result.put("server", serverInfoJson);
		//result.put("configurations", configurationsJson);
		return result;
	}

	private static String getBody(BufferedReader reader) throws IOException {
		StringBuilder buffer = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		return buffer.toString();
	}
}
