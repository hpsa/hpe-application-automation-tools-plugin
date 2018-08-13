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

package com.hpe.application.automation.tools.run;

import com.hp.octane.integrations.OctaneSDK;
import com.hpe.application.automation.tools.model.SonarAdapter;
import com.hpe.application.automation.tools.octane.Messages;
import com.hpe.application.automation.tools.octane.configuration.SonarActionFactory;
import hudson.*;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.*;


public class SonarOctaneListener extends Builder implements SimpleBuildStep {

    // these properties will be used for sonar communication
    private String sonarProjectKey;
    private String sonarToken;
    private String sonarServerUrl;

    // inject variables from job configuration if exist
    @DataBoundConstructor
    public SonarOctaneListener(String sonarProjectKey, String sonarToken, String sonarServerUrl) {
        this.sonarProjectKey = sonarProjectKey == null ? "" : sonarProjectKey;
        this.sonarToken = sonarToken == null ? "" : sonarToken;
        this.sonarServerUrl = sonarServerUrl == null ? "" : sonarServerUrl;
    }

    /**
     * get project key
     * @return
     */
    public String getSonarProjectKey() {
        return sonarProjectKey;
    }

    /**
     * get server token
     * @return
     */
    public String getSonarToken() {
        return sonarToken;
    }

    /**
     * get server url
     * @return
     */
    public String getSonarServerUrl() {
        return sonarServerUrl;
    }


    /**
     * this method is initializing sonar server details from listener configuration or
     * sonar plugin data
     * @param run current run
     * @param allConfigurations jenkins global configuration
     * @throws InterruptedException
     */
    private String[] getSonarDetails(@Nonnull Run<?, ?> run, ExtensionList<GlobalConfiguration> allConfigurations) throws InterruptedException {
        String [] serverDetails = new String[3];
        // if one of the properties is empty, need to query sonar plugin from jenkins to get the data
        if (sonarProjectKey.isEmpty() || sonarToken.isEmpty() || sonarServerUrl.isEmpty()) {
            try {
                if (allConfigurations != null) {
                    GlobalConfiguration sonarConfiguration = allConfigurations.getDynamic("hudson.plugins.sonar.SonarGlobalConfiguration");
                    if (sonarConfiguration != null) { // sonar installed, configuration exists
                        SonarAdapter adapter = new SonarAdapter(run, sonarConfiguration);
                        serverDetails[0] = sonarServerUrl.isEmpty() ? adapter.extractSonarUrl() : sonarServerUrl;
                        serverDetails[1] = sonarToken.isEmpty() ? adapter.extractSonarToken() : sonarToken;
                        serverDetails[2] = sonarProjectKey.isEmpty() ? adapter.extractSonarProjectKey() : sonarProjectKey;
                    }
                }
            } catch (Exception e) {
                throw new InterruptedException("exception occurred while init sonar tracker for job " + run.getDisplayName() + " error message: " + e.getMessage());
            }
        }
        return serverDetails;
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
        ExtensionList<GlobalConfiguration> allConfigurations = GlobalConfiguration.all();
        String callbackUrl = buildCallbaclUrl(run);
        String[] serverDetails = getSonarDetails(run, allConfigurations);
        OctaneSDK.getInstance().getSonarService().setSonarAuthentication(serverDetails[2], serverDetails[0], serverDetails[1]);
        OctaneSDK.getInstance().getSonarService().registerWebhook(callbackUrl, serverDetails[0], serverDetails[1], serverDetails[2]);
    }

    /**
     * build jenkins callback URL for sonar webhook
     * @param run
     * @return full url with job info
     */
    private String buildCallbaclUrl(Run<?, ?> run) {
        String rootUrl = Jenkins.getInstance().getRootUrl();
        String jobName = "";
        if (run instanceof AbstractBuild) {
            AbstractBuild abstractBuild = (AbstractBuild) run;
            if (abstractBuild instanceof MavenModuleSetBuild) {
                jobName = ((MavenModuleSetBuild) run).getProject().getName();
            } else {
                AbstractProject project = abstractBuild.getProject();
                if (project instanceof Project) {
                    jobName = project.getName();
                }
            }
        }
        return rootUrl + "job/" + jobName + "/webhooks/analysis/notify";
    }

    @Override
    public SonarDescriptor getDescriptor() {
        return (SonarDescriptor) super.getDescriptor();
    }


    @Extension
    public static class SonarDescriptor extends BuildStepDescriptor<Builder> {
        String sonarId = "hudson.plugins.sonar.SonarGlobalConfiguration";

        public SonarDescriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "ALM Octane SonarQube coverage listener";
        }

        /**
         * get sonar adapter. the adapter adds a dependency at compile time for sonar classes
         * and enables the usage of sonar interfaces
         * @param project
         * @return
         */
        private SonarAdapter getAdapter(AbstractProject project) {
            ExtensionList filteredExtensions = null;
            try {
                filteredExtensions = Jenkins.getInstance().getExtensionList(this.sonarId);
                GlobalConfiguration sonarConfiguration = (GlobalConfiguration) filteredExtensions.getDynamic(sonarId);
                SonarAdapter adapter = new SonarAdapter(project, sonarConfiguration);
                return adapter;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        public FormValidation doCheckSonarProjectKey(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException, ServletException {
            // if no value, validate that we can extract data from sonar configuration
            if (value.isEmpty()) {
                SonarAdapter adapter = this.getAdapter(project);
                if (adapter != null && !adapter.extractSonarProjectKey().isEmpty()) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.warning(Messages.CannotExtractSonarCoverageProjectKey());
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckSonarToken(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException, ServletException {
            if (value.isEmpty()) {
                SonarAdapter adapter = this.getAdapter(project);
                if (adapter != null && !adapter.extractSonarToken().isEmpty()) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.warning(Messages.CannotExtractSonarServerToken());
                }
            }
            return FormValidation.ok();
        }
        public FormValidation doCheckSonarServerUrl(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException, ServletException {
            if (value.isEmpty()) {
                SonarAdapter adapter = this.getAdapter(project);
                if (adapter != null && !adapter.extractSonarUrl().isEmpty()) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.warning(Messages.CannotExtractSonarURL());
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doTestConnection(@QueryParameter("sonarServerUrl") final String url, @QueryParameter("sonarToken") final String token,
                                               @QueryParameter("sonarProjectKey") final String projectKey) {
            if (url.isEmpty()) {
                return FormValidation.warning(Messages.missingSonarServerUrl());
            } else {
                boolean isConnectionWorking = OctaneSDK.getInstance().getSonarService().testConnectivity(projectKey, url, token);
                if (isConnectionWorking) {
                    return FormValidation.ok("Validation passed. Connected successfully to server " + url);
                } else {
                    return FormValidation.warning(Messages.cannotEstablishSonarConnection());
                }
            }
        }


        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

    }

}
