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


/**
 * Load runner pipeline step
 */
public class LrScenarioLoadStep extends AbstractStepImpl {



    private final RunFromFileBuilder runFromFileBuilder;

    /**
     * Gets archive run test results mode.
     *
     * @return the archive run test results mode
     */
    public String getArchiveRunTestResultsMode() {
//        return archiveRunTestResultsMode;
        return ResultsPublisherModel.CreateHtmlReportResults.getValue();
    }

    /**
     * Is publish results boolean.
     *
     * @return the boolean
     */
    public boolean isPublishResults() {
//        return publishResults;
        return true;
    }


//    private final  String archiveRunTestResultsMode;
//    private final  boolean publishResults;

    /**
     * Instantiates a new Lr scenario load step.
     *
     * @param testPaths the test paths
     */
    @DataBoundConstructor
    public LrScenarioLoadStep(String testPaths)
    {
        runFromFileBuilder = new RunFromFileBuilder(testPaths);
    }

    /**
     * Sets fs timeout.
     *
     * @param fsTimeout the fs timeout
     */
    @DataBoundSetter
    public void setFsTimeout(String fsTimeout)
    {
        runFromFileBuilder.setFsTimeout(fsTimeout);
    }

    /**
     * Sets controller polling interval.
     *
     * @param controllerPollingInterval the controller polling interval
     */
    @DataBoundSetter
    public void setControllerPollingInterval(String controllerPollingInterval)
    {
        runFromFileBuilder.setControllerPollingInterval(controllerPollingInterval);
    }

    /**
     * Sets per scenario time out.
     *
     * @param perScenarioTimeOut the per scenario time out
     */
    @DataBoundSetter
    public void setPerScenarioTimeOut(String perScenarioTimeOut)
    {
        runFromFileBuilder.setPerScenarioTimeOut(perScenarioTimeOut);
    }

    /**
     * Sets ignore error strings.
     *
     * @param ignoreErrorStrings the ignore error strings
     */
    @DataBoundSetter
    public void setIgnoreErrorStrings(String ignoreErrorStrings)
    {
        runFromFileBuilder.setIgnoreErrorStrings(ignoreErrorStrings);
    }

    /**
     * Gets controller polling interval.
     *
     * @return the controller polling interval
     */
    public String getControllerPollingInterval() {
        return runFromFileBuilder.getRunFromFileModel().getControllerPollingInterval();
    }

    /**
     * Gets fs timeout.
     *
     * @return the fs timeout
     */
    public String getFsTimeout() {
        return runFromFileBuilder.getRunFromFileModel().getFsTimeout();
    }

    /**
     * Gets per scenario time out.
     *
     * @return the per scenario time out
     */
    public String getPerScenarioTimeOut() {
        return runFromFileBuilder.getRunFromFileModel().getPerScenarioTimeOut();
    }

    /**
     * Gets test paths.
     *
     * @return the test paths
     */
    public String getTestPaths() {
        return runFromFileBuilder.getRunFromFileModel().getFsTests();
    }

    /**
     * Gets ignore error strings.
     *
     * @return the ignore error strings
     */
    public String getIgnoreErrorStrings() {
        return runFromFileBuilder.getRunFromFileModel().getIgnoreErrorStrings();
    }

    /**
     * Gets run from file builder.
     *
     * @return the run from file builder
     */
    public RunFromFileBuilder getRunFromFileBuilder() {
        return runFromFileBuilder;
    }

    /**
     * The type Descriptor.
     */
    @Extension @Symbol("LrScenarioLoad")
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        /**
         * Instantiates a new Descriptor.
         */
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

        /**
         * Gets report archive modes.
         *
         * @return the report archive modes
         */
        public List<EnumDescription> getReportArchiveModes() {

            return ResultsPublisherModel.archiveModes;
        }


    }

}
