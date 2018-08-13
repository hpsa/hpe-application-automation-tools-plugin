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

package com.hpe.application.automation.tools.octane.tests;

import com.hp.octane.integrations.OctaneSDK;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hpe.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import hudson.model.Job;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.httpclient.HttpStatus;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.HashMap;

/**
 * this class is used as an API for sonar notifications (webhook)
 */
@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872", "squid:S2925", "squid:S109", "squid:S1607"})
public class SonarApi {

	private Job job;

	public SonarApi(Job job) {
		this.job = job;
	}

	/**
	 * upon notification from sonar, we will remove sonar webhook and will execute
	 * and analysis process that fetches data from sonar API and push results to octane
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	public void doNotify(StaplerRequest req, StaplerResponse res) throws IOException {
		JSONObject inputNotification = (JSONObject) JSONValue.parse(req.getInputStream());
		Object properties = inputNotification.get("properties");
		// without build context, could not send octane relevant data
		if (properties != null && properties instanceof HashMap) {
			// get relevant parameters
			HashMap sonarAttachedProperties = ((HashMap) inputNotification.get("properties"));
			String buildNumber = (String) sonarAttachedProperties.get("sonar.analysis.buildNumber");
			String jobName = JobProcessorFactory.getFlowProcessor(job).getTranslateJobName();
			String serverIdentity = ConfigurationService.getModel().getIdentity();
			HashMap project = (HashMap) inputNotification.get("project");
			String sonarProjectKey = (String) project.get("key");
			// use SDK to fetch and push data
			OctaneSDK.getInstance().getSonarService().unregisterWebhook(sonarProjectKey);
			OctaneSDK.getInstance().getSonarService().injectSonarDataToOctane(sonarProjectKey, serverIdentity, jobName, buildNumber);
		}
		res.setStatus(HttpStatus.SC_OK);
	}

}
