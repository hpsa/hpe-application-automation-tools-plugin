package com.hp.application.automation.tools.pipelineSteps;


import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.ResultsPublisherModel;
import com.hp.application.automation.tools.run.RunFromFileBuilder;
import hudson.Extension;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.List;

public class LrScenarioLoadStep extends AbstractStepImpl {



    private final RunFromFileBuilder runFromFileBuilder;

    public String getArchiveRunTestResultsMode() {
//        return archiveRunTestResultsMode;
        return ResultsPublisherModel.CreateHtmlReportResults.toString();
    }

    public boolean isPublishResults() {
//        return publishResults;
        return true;
    }


//    private final  String archiveRunTestResultsMode;
//    private final  boolean publishResults;

    @DataBoundConstructor
    public LrScenarioLoadStep(String testPaths)
    {
        runFromFileBuilder = new RunFromFileBuilder(testPaths);
    }

    @DataBoundSetter
    public void setFsTimeout(String fsTimeout)
    {
        runFromFileBuilder.setFsTimeout(fsTimeout);
    }

    @DataBoundSetter
    public void setControllerPollingInterval(String controllerPollingInterval)
    {
        runFromFileBuilder.setControllerPollingInterval(controllerPollingInterval);
    }

    @DataBoundSetter
    public void setPerScenarioTimeOut(String perScenarioTimeOut)
    {
        runFromFileBuilder.setPerScenarioTimeOut(perScenarioTimeOut);
    }

    @DataBoundSetter
    public void setIgnoreErrorStrings(String ignoreErrorStrings)
    {
        runFromFileBuilder.setIgnoreErrorStrings(ignoreErrorStrings);
    }

    public String getControllerPollingInterval() {
        return runFromFileBuilder.getRunFromFileModel().getControllerPollingInterval();
    }

    public String getFsTimeout() {
        return runFromFileBuilder.getRunFromFileModel().getFsTimeout();
    }

    public String getPerScenarioTimeOut() {
        return runFromFileBuilder.getRunFromFileModel().getPerScenarioTimeOut();
    }

    public String getTestPaths() {
        return runFromFileBuilder.getRunFromFileModel().getFsTests();
    }

    public String getIgnoreErrorStrings() {
        return runFromFileBuilder.getRunFromFileModel().getIgnoreErrorStrings();
    }

    public RunFromFileBuilder getRunFromFileBuilder() {
        return runFromFileBuilder;
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
