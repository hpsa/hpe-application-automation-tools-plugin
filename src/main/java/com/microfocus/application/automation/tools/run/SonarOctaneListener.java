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

package com.microfocus.application.automation.tools.run;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.exceptions.SonarIntegrationException;
import com.microfocus.application.automation.tools.octane.model.SonarHelper;
import com.microfocus.application.automation.tools.octane.actions.WebhookAction;
import com.microfocus.application.automation.tools.octane.actions.Webhooks;
import com.microfocus.application.automation.tools.sse.common.StringUtils;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class SonarOctaneListener extends Builder implements SimpleBuildStep {

    // these properties will be used for sonar communication
    public String sonarToken;
    public String sonarServerUrl;
    boolean pushVulnerabilities;
    boolean pushCoverage;

    private Set<SonarHelper.DataType> dataTypeSet = new HashSet<>();


    @DataBoundConstructor
    public SonarOctaneListener() {
    }

    @DataBoundSetter
    public void setSonarToken(String sonarToken) {
        this.sonarToken = sonarToken;
    }

    @DataBoundSetter
    public void setSonarServerUrl(String sonarServerUrl) {
        this.sonarServerUrl = sonarServerUrl;
    }

    @DataBoundSetter
    public void setPushVulnerabilities(boolean pushVulnerabilities) {
        this.pushVulnerabilities = pushVulnerabilities;
        if (pushVulnerabilities) {
            dataTypeSet.add(SonarHelper.DataType.VULNERABILITIES);
        } else {
            dataTypeSet.remove(SonarHelper.DataType.VULNERABILITIES);
        }

    }

    @DataBoundSetter
    public void setPushCoverage(boolean pushCoverage) {
        this.pushCoverage = pushCoverage;
        if (pushCoverage) {
            dataTypeSet.add(SonarHelper.DataType.COVERAGE);
        } else {
            dataTypeSet.remove(SonarHelper.DataType.COVERAGE);
        }
    }

    /**
     * get server token
     *
     * @return
     */
    public String getSonarToken() {
        return sonarToken;
    }

    /**
     * get server url
     *
     * @return
     */
    public String getSonarServerUrl() {
        return sonarServerUrl;
    }

    /**
     * is Push Vulnerabilities to octane
     *
     * @return
     */
    public boolean isPushVulnerabilities() {
        return pushVulnerabilities;
    }

    /**
     * is Push Coverage to octane
     *
     * @return
     */
    public boolean isPushCoverage() {
        return pushCoverage;
    }

    /**
     * this method is initializing sonar server details from listener configuration or
     * sonar plugin data
     *
     * @param run current run
     * @throws InterruptedException
     */
    private void initializeSonarDetails(@Nonnull Run<?, ?> run, TaskListener listener) throws InterruptedException {
        // if one of the properties is empty, need to query sonar plugin from jenkins to get the data
        ExtensionList<GlobalConfiguration> allConfigurations = GlobalConfiguration.all();
        if (allConfigurations != null) {
            SonarHelper adapter = new SonarHelper(run, listener);
            // get the most recent sonar server details from  environments variables, and only if there is no details there take the details from the class properties.
            setSonarServerUrl(StringUtils.isNullOrEmpty(adapter.getServerUrl()) ? sonarServerUrl : adapter.getServerUrl());
            setSonarToken(StringUtils.isNullOrEmpty(adapter.getServerToken()) ? sonarToken : adapter.getServerToken());
        }
    }


    private String getBuildNumber(Run<?, ?> run) {
        if (run instanceof AbstractBuild) {
            AbstractBuild abstractBuild = (AbstractBuild) run;
            return String.valueOf(abstractBuild.getNumber());
        }
        return "";
    }

    /**
     * Run this step.
     *
     * @param run       a build this is running as a part of
     * @param workspace a workspace to use for any file operations
     * @param launcher  a way to start processes
     * @param listener  a place to send output
     * @throws InterruptedException if the step is interrupted
     */
    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        initializeSonarDetails(run, listener);

        String jenkinsRoot = Jenkins.get().getRootUrl();
        String callbackWebHooksURL = jenkinsRoot + Webhooks.WEBHOOK_PATH + Webhooks.NOTIFY_METHOD;
        if (StringUtils.isNullOrEmpty(this.sonarServerUrl) || StringUtils.isNullOrEmpty(this.sonarToken)) {
            logger.println("Web-hook registration in sonarQube for build " + getBuildNumber(run) + " failed, missing sonarQube server url or sonarQube authentication token");
        } else {
            logger.println("callback URL for jenkins resource will be set to: " + callbackWebHooksURL + " in sonarQube server with URL: " + this.sonarServerUrl);
            OctaneSDK.getClients().forEach(octaneClient -> {
                try {
                    octaneClient.getSonarService().ensureSonarWebhookExist(callbackWebHooksURL, getSonarServerUrl(), getSonarToken());
                } catch (SonarIntegrationException e) {
                    logger.println("Web-hook registration in sonarQube for build " + getBuildNumber(run) + " failed: " + e.getMessage());
                }
            });
            run.addAction(new WebhookAction(true, getSonarServerUrl(), dataTypeSet));
        }
    }

    @Override
    public SonarDescriptor getDescriptor() {
        return (SonarDescriptor) super.getDescriptor();
    }

    @Symbol("addALMOctaneSonarQubeListener")
    @Extension
    public static class SonarDescriptor extends BuildStepDescriptor<Builder> {

        public SonarDescriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "ALM Octane SonarQube listener";
        }


        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
    }
}
