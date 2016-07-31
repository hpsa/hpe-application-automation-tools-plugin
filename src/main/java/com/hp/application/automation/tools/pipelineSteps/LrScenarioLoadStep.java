package com.hp.application.automation.tools.pipelineSteps;


import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.ResultsPublisherModel;
import com.hp.application.automation.tools.results.RunResultRecorder;
import com.hp.application.automation.tools.run.RunFromFileBuilder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class LrScenarioLoadStep extends AbstractStepImpl {

    public String getArchiveTestResultsMode() {
        return archiveTestResultsMode;
    }

    public boolean isPublishResults() {
        return publishResults;
    }

    public String getIgnoreErrorStrings() {
        return ignoreErrorStrings;
    }

    public String getPerScenarioTimeOut() {
        return perScenarioTimeOut;
    }

    public String getControllerPollingInterval() {
        return controllerPollingInterval;
    }

    public String getFsTimeout() {
        return fsTimeout;
    }

    public String getFsTests() {
        return fsTests;
    }

    private final  String archiveTestResultsMode;
    private final  boolean publishResults;
    private final  String ignoreErrorStrings;
    private final  String perScenarioTimeOut;
    private final  String controllerPollingInterval;
    private final  String fsTimeout;
    private final  String fsTests;

    @DataBoundConstructor
    public LrScenarioLoadStep(String fsTests, String fsTimeout, String controllerPollingInterval,
                              String perScenarioTimeOut, String ignoreErrorStrings, boolean publishResults, String archiveTestResultsMode)
    {
        this.fsTests = fsTests;
        this.fsTimeout = fsTimeout;
        this.controllerPollingInterval = controllerPollingInterval;
        this.perScenarioTimeOut = perScenarioTimeOut;
        this.ignoreErrorStrings = ignoreErrorStrings;
        this.publishResults = publishResults;
        this.archiveTestResultsMode = archiveTestResultsMode;
    }
    
//    public final void startScenarioLoad(Run<?, ?> build, FilePath ws, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
//
//        RunFromFileBuilder runFromFileBuilder = new RunFromFileBuilder(getFsTests(), getFsTimeout(), getControllerPollingInterval(), getPerScenarioTimeOut(), getIgnoreErrorStrings(), "", "", "", "", "", "", "", "", "", "", "", "", "", null, false);
//        RunResultRecorder runResultRecorder = new RunResultRecorder(isPublishResults(), getArchiveTestResultsMode());
//
//        runFromFileBuilder.perform(build, ws, launcher, listener);
//        runResultRecorder.perform(build, ws, launcher, listener);
//    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() { super(LrScenarioLoadStepExecutor.class); }

        @Override
        public String getFunctionName() {
            return "RunLoadRunnerScenario";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Run LoadRunner scenario";
        }

        public List<EnumDescription> getReportArchiveModes() {

            return ResultsPublisherModel.archiveModes;
        }
    }

}
