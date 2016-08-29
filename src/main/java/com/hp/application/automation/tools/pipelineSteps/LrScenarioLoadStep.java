package com.hp.application.automation.tools.pipelineSteps;


import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.ResultsPublisherModel;
import hudson.Extension;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;

public class LrScenarioLoadStep extends AbstractStepImpl {

    public String getArchiveRunTestResultsMode() {
//        return archiveRunTestResultsMode;
        return ResultsPublisherModel.CreateHtmlReportResults.toString();
    }

    public boolean isPublishResults() {
//        return publishResults;
        return true;
    }

    public String getIgnoreErrorStrings() {
        return ignoreErrorStrings;
    }

    public String getPerScenarioRunTimeOut() {
        return perScenarioRunTimeOut;
    }

    public String getControllerRunPollingInterval() {
        return controllerRunPollingInterval;
    }

    public String getRunTimeout() {
        return runTimeout;
    }

    public String getTestPaths() {
        return testPaths;
    }

//    private final  String archiveRunTestResultsMode;
//    private final  boolean publishResults;
    private final  String ignoreErrorStrings;
    private final  String perScenarioRunTimeOut;
    private final  String controllerRunPollingInterval;
    private final  String runTimeout;
    private final  String testPaths;

//    @DataBoundConstructor
//    public LrScenarioLoadStep(String testPaths, String runTimeout, String controllerRunPollingInterval,
//                              String perScenarioRunTimeOut, String ignoreErrorStrings, boolean publishResults, String archiveRunTestResultsMode)
//    {
//        this.testPaths = testPaths;
//        this.runTimeout = runTimeout;
//        this.controllerRunPollingInterval = controllerRunPollingInterval;
//        this.perScenarioRunTimeOut = perScenarioRunTimeOut;
//        this.ignoreErrorStrings = ignoreErrorStrings;
//        this.publishResults = publishResults;
//        this.archiveRunTestResultsMode = archiveRunTestResultsMode;
//    }

    @DataBoundConstructor
    public LrScenarioLoadStep(String controllerRunPollingInterval, String ignoreErrorStrings, String perScenarioRunTimeOut, String testPaths, String runTimeout)
    {
        this.testPaths = testPaths;
        this.runTimeout = runTimeout;
        this.controllerRunPollingInterval = controllerRunPollingInterval;
        this.perScenarioRunTimeOut = perScenarioRunTimeOut;
        this.ignoreErrorStrings = ignoreErrorStrings;
    }

    @Extension @Symbol("LrScenarioLoad")
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() { super(LrScenarioLoadStepExecution.class); }

        @Override
        public String getFunctionName() {
            return "lrScenarioLoad";
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
