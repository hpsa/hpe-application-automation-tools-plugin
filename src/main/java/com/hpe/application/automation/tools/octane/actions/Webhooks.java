/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.octane.actions;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.api.TasksProcessor;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.connectivity.HttpMethod;
import com.hp.octane.integrations.dto.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.connectivity.OctaneTaskAbridged;
import com.hp.octane.integrations.exceptions.OctaneSDKSonarException;
import com.hpe.application.automation.tools.octane.configuration.ConfigApi;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hpe.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import hudson.Extension;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
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
public class Webhooks implements RootAction {
	private static final Logger logger = LogManager.getLogger(Webhooks.class);

	// params for extracting information from json
	private final String BUILD_NUMBER_PARAM_NAME = "sonar.analysis.buildNumber";
    private final String SONAR_PROJECT_KEY_NAME = "key";
    private final String JOB_NAME_PARAM_NAME = "sonar.analysis.jobName";
    private final String PROJECT = "project";

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "webhooks";
	}

	public ConfigApi getConfiguration() {
		return new ConfigApi();
	}

	public void doNotify(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		JSONObject inputNotification = (JSONObject) JSONValue.parse(req.getInputStream());
		Object properties = inputNotification.get("properties");
		// without build context, could not send octane relevant data
		if (properties != null && properties instanceof HashMap) {
			// get relevant parameters
			HashMap sonarAttachedProperties = ((HashMap) properties);
			String buildNumber = (String) sonarAttachedProperties.get(BUILD_NUMBER_PARAM_NAME);
			String jobName = (String) sonarAttachedProperties.get(JOB_NAME_PARAM_NAME);
			// Jenkins.getInstance().getItem(jobName)
			String serverIdentity = ConfigurationService.getModel().getIdentity();
			HashMap project = (HashMap) inputNotification.get(PROJECT);
			String sonarProjectKey = (String) project.get(SONAR_PROJECT_KEY_NAME);

			// use SDK to fetch and push data
			try {
				// OctaneSDK.getInstance().getSonarService().unregisterWebhook(sonarProjectKey, jobName);
				OctaneSDK.getInstance().getSonarService().injectSonarDataToOctane(sonarProjectKey, serverIdentity, jobName, buildNumber);
			} catch (OctaneSDKSonarException e) {
				throw new IOException(e.getMessage());
			} finally {
				res.setStatus(HttpStatus.SC_OK);
			}
		}
	}

}
