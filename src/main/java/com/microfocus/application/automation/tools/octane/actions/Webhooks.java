/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.actions;

import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.services.vulnerabilities.ToolType;
import com.microfocus.application.automation.tools.octane.ImpersonationUtil;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.model.SonarHelper;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import com.microfocus.application.automation.tools.octane.vulnerabilities.VulnerabilitiesUtils;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.UnprotectedRootAction;
import hudson.security.ACLContext;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 8/10/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class Webhooks implements UnprotectedRootAction {
    private static final Logger logger = SDKBasedLoggerProvider.getLogger(Webhooks.class);
    // url details
    public static final String WEBHOOK_PATH = "webhooks";
    public static final String NOTIFY_METHOD = "/notify";

    private String PROJECT_KEY_KEY = "PROJECT_KEY";
    private String SONAR_URL_KEY = "SONAR_URL";
    private String SONAR_TOKEN_KEY = "SONAR_TOKEN";
    private String REMOTE_TAG_KEY = "REMOTE_TAG";

    // json parameter names
    private final String PROJECT = "project";
    private final String SONAR_PROJECT_KEY_NAME = "key";
    private final String IS_EXPECTING_FILE_NAME = "is_expecting.txt";
    private final String JOB_NAME_PARAM_NAME = "sonar.analysis.jobName";
    private final String BUILD_NUMBER_PARAM_NAME = "sonar.analysis.buildNumber";
    private static final String PROJECT_KEY_HEADER = "X-SonarQube-Project";

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return WEBHOOK_PATH;
    }

    @RequirePOST
    public void doNotify(StaplerRequest req, StaplerResponse res) throws IOException {
        logger.info("Received POST from " + req.getRemoteHost());
        // legal user, handle request
        JSONObject inputNotification = (JSONObject) JSONValue.parse(req.getInputStream());
        Object properties = inputNotification.get("properties");

        ExtensionList<GlobalConfiguration> allConfigurations = GlobalConfiguration.all();
        GlobalConfiguration sonarConfiguration = allConfigurations.getDynamic(SonarHelper.SONAR_GLOBAL_CONFIG);

        // without build context, could not send octane relevant data
        if (sonarConfiguration != null && !req.getHeader(PROJECT_KEY_HEADER).isEmpty() && properties instanceof Map) {
            // get relevant parameters
            Map sonarAttachedProperties = (Map) properties;
            // filter notifications from sonar projects, who haven't configured listener parameters
            if (sonarAttachedProperties.containsKey(BUILD_NUMBER_PARAM_NAME) && sonarAttachedProperties.containsKey(JOB_NAME_PARAM_NAME)) {
                String jobName = (String) sonarAttachedProperties.get(JOB_NAME_PARAM_NAME);
                String buildIdStr = (String) (sonarAttachedProperties.get(BUILD_NUMBER_PARAM_NAME));
                int buildId;
                try {
                    buildId = Integer.parseInt(buildIdStr);
                } catch (NumberFormatException e) {
                    logger.warn("Got request from sonarqube webhook listener, but buildIdStr is illegal : " + buildIdStr);
                    res.setStatus(HttpStatus.SC_NOT_ACCEPTABLE);
                    return;
                }

                Run run = null;
                for (OctaneClient octaneClient : OctaneSDK.getClients()) {
                    try {
                        if (octaneClient.getConfigurationService().getConfiguration().isDisabled()) {
                            continue;
                        }
                        Job jenkinsJob = getJob(octaneClient, jobName);
                        if (jenkinsJob == null) {
                            continue;
                        }
                        run = jenkinsJob.getBuildByNumber(buildId);
                        if (run == null) {
                            logger.warn("Got request from sonarqube webhook listener, but build " + buildIdStr + " context could not be resolved");
                            res.setStatus(HttpStatus.SC_NOT_ACCEPTABLE);
                            return;
                        }
                        if (!isRunExpectingToGetWebhookCall(run) || isRunAlreadyGotWebhookCall(run)) {
                            return;
                        }

                        //enqueue coverage and vulnerabilities
                        WebhookAction action = run.getAction(WebhookAction.class);
                        String parents = BuildHandlerUtils.getRootJobCiIds(run);
                        String sonarToken = SonarHelper.getSonarInstallationTokenByUrl(sonarConfiguration, action.getServerUrl(), run);
                        HashMap project = (HashMap) inputNotification.get(PROJECT);
                        String sonarProjectKey = (String) project.get(SONAR_PROJECT_KEY_NAME);
                        String ciJobId = BuildHandlerUtils.translateFolderJobName(jobName);

                        if (action.getDataTypeSet().contains(SonarHelper.DataType.COVERAGE)) {
                            // use SDK to fetch and push data
                            octaneClient.getSonarService().enqueueFetchAndPushSonarCoverage(ciJobId, buildIdStr, sonarProjectKey, action.getServerUrl(), sonarToken, parents);
                        }
                        if (action.getDataTypeSet().contains(SonarHelper.DataType.VULNERABILITIES)) {
                            Map<String, String> additionalProperties = new HashMap<>();
                            additionalProperties.put(PROJECT_KEY_KEY, sonarProjectKey);
                            additionalProperties.put(SONAR_URL_KEY, action.getServerUrl());
                            additionalProperties.put(SONAR_TOKEN_KEY, sonarToken);
                            additionalProperties.put(REMOTE_TAG_KEY, sonarProjectKey);
                            octaneClient.getVulnerabilitiesService().enqueueRetrieveAndPushVulnerabilities(ciJobId, buildIdStr, ToolType.SONAR, run.getStartTimeInMillis(),
                                    VulnerabilitiesUtils.getFortifyTimeoutHours(octaneClient.getInstanceId()), additionalProperties, parents);

                        }
                        res.setStatus(HttpStatus.SC_OK); // sonar should get positive feedback for webhook

                    } catch (Exception e) {
                        logger.error("exception occurred while trying to enqueue fetchAndPush task to octane, clientId: " + octaneClient.getInstanceId() + "" +
                                ", jobName: " + jobName + ", build: " + buildIdStr + ",", e);
                    }

                    if (run != null) {
                        markBuildAsReceivedWebhookCall(run);
                    }
                }
            }
        }
    }

    private Job getJob(OctaneClient octaneClient, String jobName) {
        ACLContext aclContext = null;
        try {
            aclContext = ImpersonationUtil.startImpersonation(octaneClient.getInstanceId(), null);
            Item topLevelItem = Jenkins.get().getItemByFullName(jobName);
            if (topLevelItem != null && topLevelItem instanceof Job) {
                Job jenkinsJob = ((Job) topLevelItem);
                return jenkinsJob;
            } else {
                return null;
            }
        } finally {
            if (aclContext != null) {
                ImpersonationUtil.stopImpersonation(aclContext);
            }
        }
    }

    /**
     * this method checks if run already got webhook call.
     * we are only handling the first call, laters call for the same run
     * will be rejected
     *
     * @param run run
     * @return result
     */
    private Boolean isRunAlreadyGotWebhookCall(Run run) {
        try {
            // run is promised to be exist at this point
            File rootDir = run.getRootDir();
            File isExpectingFile = new File(rootDir, IS_EXPECTING_FILE_NAME);
            FileInputStream fis = new FileInputStream(isExpectingFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return (Boolean) ois.readObject();
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    /**
     * use build action to decide whether we need to get a webhook call from sonarqube
     *
     * @param run build
     * @return true or false
     */
    private Boolean isRunExpectingToGetWebhookCall(Run run) {
        WebhookAction action = run.getAction(WebhookAction.class);
        return action != null && action.getExpectingToGetWebhookCall();
    }

    /**
     * this method persist the fact a specific run got webhook call.
     *
     * @param run run
     * @throws IOException exception
     */
    private void markBuildAsReceivedWebhookCall(Run run) throws IOException {
        if (run == null) {
            return;
        }
        File buildBaseFolder = run.getRootDir();
        File isExpectingFile = new File(buildBaseFolder, IS_EXPECTING_FILE_NAME);
        FileOutputStream fos = new FileOutputStream(isExpectingFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(true);
    }
}
