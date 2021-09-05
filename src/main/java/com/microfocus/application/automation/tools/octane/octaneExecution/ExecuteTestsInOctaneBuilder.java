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

package com.microfocus.application.automation.tools.octane.octaneExecution;

import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.octaneExecution.ExecutionMode;
import com.hp.octane.integrations.services.testexecution.TestExecutionService;
import com.microfocus.application.automation.tools.octane.JellyUtils;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Execute tests in Octane
 */
public class ExecuteTestsInOctaneBuilder extends Builder implements SimpleBuildStep {

    private String configurationId;
    private String executionMode;
    private String ids;
    private String workspaceId;

    @DataBoundConstructor
    public ExecuteTestsInOctaneBuilder(String configurationId, String workspaceId, String executionMode, String ids) {
        this.configurationId = JellyUtils.NONE.equalsIgnoreCase(configurationId) ? null : configurationId;
        this.workspaceId = JellyUtils.NONE.equalsIgnoreCase(workspaceId) ? null : workspaceId;
        this.executionMode = executionMode;
        this.ids = ids;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        if (configurationId == null) {
            throw new IllegalArgumentException("ALM Octane configuration is not defined.");
        }
        if (workspaceId == null) {
            throw new IllegalArgumentException("ALM Octane workspace is not defined.");
        }

        String myConfigurationId = configurationId;
        String myWorkspaceId = workspaceId;
        String myExecutionMode = executionMode;
        String myIds = ids;
        try {
            EnvVars env = build.getEnvironment(listener);
            myConfigurationId = env.expand(configurationId);
            myWorkspaceId = env.expand(workspaceId);
            myExecutionMode = env.expand(executionMode);
            myIds = env.expand(ids);
        } catch (IOException | InterruptedException e) {
            listener.error("Failed loading build environment " + e);
        }

        Long myWorkspaceIdAsLong = null;
        try {
            myWorkspaceIdAsLong = Long.parseLong(myWorkspaceId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert workspace to long :  " + myWorkspaceId);
        }

        OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(myConfigurationId);
        TestExecutionService testExecutionService = octaneClient.getTestExecutionService();
        List<Long> suiteIds = Arrays.stream(myIds.split(",")).map(str -> Long.parseLong(str.trim())).collect(Collectors.toList());

        ParametersAction parameterAction = build.getAction(ParametersAction.class);
        Long optionalReleaseId = null;
        String optionalSuiteRunName = null;
        if (parameterAction != null) {
            optionalReleaseId = getLongValueParameter(parameterAction, "octane_release_id");
            optionalSuiteRunName = getStringValueParameter(parameterAction, "octane_new_suite_run_name");
        }
        testExecutionService.executeSuiteRuns(myWorkspaceIdAsLong, suiteIds, optionalReleaseId, optionalSuiteRunName);
    }

    private Long getLongValueParameter(ParametersAction parameterAction, String paramName) {
        ParameterValue pv = parameterAction.getParameter(paramName);
        if (pv != null && pv.getValue() instanceof String) {
            try {
                return Long.valueOf((String) pv.getValue());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String getStringValueParameter(ParametersAction parameterAction, String paramName) {
        ParameterValue pv = parameterAction.getParameter(paramName);
        if (pv != null && pv.getValue() instanceof String) {
            return (String) pv.getValue();
        }
        return null;
    }

    private static void printToConsole(TaskListener listener, String msg) {
        listener.getLogger().println(ExecuteTestsInOctaneBuilder.class.getSimpleName() + " : " + msg);
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public String getIds() {
        return ids;
    }


    @Symbol("executeTestsFromAlmOctane")
    @Extension
    public static class Descriptor extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return false;//FreeStyleProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return "Execute tests from Alm Octane";
        }


        public ListBoxModel doFillExecutionModeItems() {
            ListBoxModel m = new ListBoxModel();
            for (ExecutionMode mode : ExecutionMode.values()) {
                m.add(mode.description(), mode.value());
            }

            return m;
        }

        public ListBoxModel doFillConfigurationIdItems() {
            return JellyUtils.fillConfigurationIdModel();
        }

        public ListBoxModel doFillWorkspaceIdItems(@QueryParameter String configurationId, @QueryParameter(value = "workspaceId") String workspaceId) {
            return JellyUtils.fillWorkspaceModel(configurationId, workspaceId);
        }

    }

}
