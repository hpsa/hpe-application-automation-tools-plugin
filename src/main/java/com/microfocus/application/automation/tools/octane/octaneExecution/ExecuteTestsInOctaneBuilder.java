/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
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
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hp.octane.integrations.dto.parameters.CIParameters;
import com.hp.octane.integrations.exceptions.ConfigurationException;
import com.hp.octane.integrations.exceptions.PermissionException;
import com.hp.octane.integrations.octaneExecution.ExecutionMode;
import com.hp.octane.integrations.services.SupportsConsoleLog;
import com.hp.octane.integrations.services.testexecution.TestExecutionContext;
import com.hp.octane.integrations.services.testexecution.TestExecutionService;
import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.microfocus.application.automation.tools.octane.ImpersonationUtil;
import com.microfocus.application.automation.tools.octane.JellyUtils;
import com.microfocus.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import com.microfocus.application.automation.tools.octane.testrunner.TestsToRunConverterBuilder;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import hudson.security.ACLContext;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
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
        SupportsConsoleLog supportsConsoleLog = new SupportsConsoleLogImpl(listener);
        supportsConsoleLog.println("Start **********************************************************************************************");
        supportsConsoleLog.println("");
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

        Long myWorkspaceIdAsLong;
        try {
            myWorkspaceIdAsLong = Long.parseLong(myWorkspaceId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert workspace to long :  " + myWorkspaceId);
        }

        OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(myConfigurationId);
        TestExecutionService testExecutionService = octaneClient.getTestExecutionService();
        List<Long> suiteIds = Arrays.stream(myIds.split(","))
                .map(str -> str.trim()).filter(str -> StringUtils.isNotEmpty(str) && StringUtils.isNumeric(str))
                .map(str -> Long.parseLong(str.trim())).distinct().collect(Collectors.toList());
        try {
            testExecutionService.validateAllSuiteIdsExistAndReturnSuiteNames(myWorkspaceIdAsLong, suiteIds);
        } catch (IllegalArgumentException e) {
            listener.error(e.getMessage());
            build.setResult(Result.FAILURE);
            return;
        }
        ParametersAction parameterAction = build.getAction(ParametersAction.class);

        switch (ExecutionMode.fromValue(myExecutionMode)) {
            case SUITE_RUNS_IN_OCTANE:

                Long optionalReleaseId = getLongValueParameter(parameterAction, "octane_release_id");
                String optionalSuiteRunName = getStringValueParameter(parameterAction, "octane_new_suite_run_name");
                testExecutionService.executeSuiteRuns(myWorkspaceIdAsLong, suiteIds, optionalReleaseId, optionalSuiteRunName, supportsConsoleLog);
                break;

            case SUITE_IN_CI:
                List<TestExecutionContext> testExecutions = testExecutionService.prepareTestExecutionForSuites(myWorkspaceIdAsLong, suiteIds, supportsConsoleLog);
                ACLContext securityContext = ImpersonationUtil.startImpersonation(myConfigurationId, null);
                Map<TestExecutionContext, QueueTaskFuture<AbstractBuild>> futures = new HashMap<>();
                try {

                    testExecutions.forEach(testExecution -> {
                        AbstractProject project = getJobFromTestRunner(testExecution);
                        try {
                            supportsConsoleLog.print(String.format("%s %s, triggering test runner '%s' (%s tests): "
                                    , testExecution.getIdentifierType().getName()
                                    , testExecution.getIdentifier()
                                    , testExecution.getTestRunner().getName()
                                    , testExecution.getTests().size()
                            ));
                            listener.hyperlink("/job/" + project.getFullName().replace("/", "/job/"), project.getFullName());
                            supportsConsoleLog.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to print link to triggered job : " + e.getMessage());
                        }
                        int delay = 0;

                        Cause cause = new Cause.UpstreamCause(build);
                        CIParameter testsToRunParam = DTOFactory.getInstance().newDTO(CIParameter.class)
                                .setName(TestsToRunConverterBuilder.TESTS_TO_RUN_PARAMETER)
                                .setValue(testExecution.getTestsToRun())
                                .setType(CIParameterType.STRING);
                        CIParameters ciParams = DTOFactory.getInstance().newDTO(CIParameters.class);
                        ciParams.setParameters(Collections.singletonList(testsToRunParam));
                        ParametersAction parametersAction = new ParametersAction(CIJenkinsServicesImpl.createParameters(project, ciParams));

                        QueueTaskFuture<AbstractBuild> future = project.scheduleBuild2(delay, cause, parametersAction);
                        futures.put(testExecution, future);
                    });
                } finally {
                    ImpersonationUtil.stopImpersonation(securityContext);
                }

                //WAIT UNTIL ALL JOBS ARE FINISHED and set build result based on worse result
                supportsConsoleLog.println("Waiting for test runners to finish ... ");
                Result buildResult = Result.SUCCESS;
                for (Map.Entry<TestExecutionContext, QueueTaskFuture<AbstractBuild>> entry : futures.entrySet()) {
                    try {

                        //TODO check status
                        AbstractBuild buildFromFuture = entry.getValue().get();
                        try {
                            supportsConsoleLog.print("Build ");
                            String url = "/job/" + buildFromFuture.getProject().getFullName().replace("/", "/job/") + "/" + buildFromFuture.getNumber();
                            listener.hyperlink(url, buildFromFuture.getProject().getFullName() + " " + buildFromFuture.getDisplayName());
                            supportsConsoleLog.append(" - " + buildFromFuture.getResult());
                            supportsConsoleLog.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to print link to triggered job : " + e.getMessage());
                        }

                        if (buildFromFuture.getResult().isWorseThan(buildResult)) {
                            buildResult = buildFromFuture.getResult();
                        }

                    } catch (Exception e) {
                        //TODO CHECK WHAT TO DO HERE
                        throw new RuntimeException("Failed in waiting for job finishing : " + e.getMessage());
                    }
                }
                if (buildResult.isWorseThan(Result.SUCCESS)) {
                    build.setResult(buildResult);
                }
                break;
            default:
                throw new RuntimeException("not supported execution mode");
        }
    }

    private AbstractProject getJobFromTestRunner(TestExecutionContext testExecution) {
        String ciJobName = testExecution.getTestRunner().getEntityValue("ci_job").getName();
        Job job = (Job) Jenkins.get().getItemByFullName(ciJobName);
        if (job != null) {
            if (job instanceof AbstractProject && ((AbstractProject) job).isDisabled()) {
                //disabled job is not runnable and in this context we will handle it as 404
                throw new ConfigurationException("Job is disabled " + ciJobName, HttpStatus.SC_NOT_FOUND);
            }
            boolean hasBuildPermission = job.hasPermission(Item.BUILD);
            if (!hasBuildPermission) {
                throw new PermissionException("No permission to run job " + ciJobName, HttpStatus.SC_FORBIDDEN);
            }
            if (job instanceof AbstractProject || job.getClass().getName().equals(JobProcessorFactory.WORKFLOW_JOB_NAME)) {

            }
        } else {
            throw new ConfigurationException("Job is not found " + ciJobName, HttpStatus.SC_NOT_FOUND);
        }

        return (AbstractProject) job;
    }

    private Long getLongValueParameter(ParametersAction parameterAction, String paramName) {
        if (parameterAction == null) {
            return null;
        }
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
        if (parameterAction == null) {
            return null;
        }
        ParameterValue pv = parameterAction.getParameter(paramName);
        if (pv != null && pv.getValue() instanceof String) {
            return (String) pv.getValue();
        }
        return null;
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

    public static class SupportsConsoleLogImpl implements SupportsConsoleLog {
        TaskListener listener;

        public SupportsConsoleLogImpl(TaskListener listener) {
            this.listener = listener;
        }

        public void println(String msg) {
            listener.getLogger().println(ExecuteTestsInOctaneBuilder.class.getSimpleName() + " : " + msg);
        }

        public void print(String msg) {
            listener.getLogger().print(ExecuteTestsInOctaneBuilder.class.getSimpleName() + " : " + msg);
        }

        public void append(String msg) {
            listener.getLogger().print(msg);
        }

        public void newLine() {
            listener.getLogger().println();
        }
    }

    @Symbol("executeTestsFromAlmOctane")
    @Extension
    public static class Descriptor extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;//FreeStyleProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return "Execute tests from ALM Octane (Tech Preview)";
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
