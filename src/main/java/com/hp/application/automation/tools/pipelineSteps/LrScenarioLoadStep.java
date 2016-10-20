/*
 * MIT License
 *
 * Copyright (c) 2016 Hewlett-Packard Development Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
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
 */

package com.hp.application.automation.tools.pipelineSteps;

import com.hp.application.automation.tools.model.EnumDescription;
import com.hp.application.automation.tools.model.ResultsPublisherModel;
import com.hp.application.automation.tools.results.RunResultRecorder;
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
    private final RunResultRecorder runResultRecorder;

    /**
     * Instantiates a new Lr scenario load step.
     *
     * @param testPaths              the test paths
     * @param archiveTestResultsMode the type of archiving the user wants.
     */
    @DataBoundConstructor
    public LrScenarioLoadStep(String testPaths, String archiveTestResultsMode) {
        this.runFromFileBuilder = new RunFromFileBuilder(testPaths);
        this.runResultRecorder = new RunResultRecorder(archiveTestResultsMode);
    }

    /**
     * Gets archive run test results mode.
     *
     * @return the archive run test results mode
     */
    public String getArchiveTestResultsMode() {
        return runResultRecorder.getResultsPublisherModel().getArchiveTestResultsMode();
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
     * Sets controller polling interval.
     *
     * @param controllerPollingInterval the controller polling interval
     */
    @DataBoundSetter
    public void setControllerPollingInterval(String controllerPollingInterval) {
        runFromFileBuilder.setControllerPollingInterval(controllerPollingInterval);
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
     * Sets fs timeout.
     *
     * @param fsTimeout the fs timeout
     */
    @DataBoundSetter
    public void setFsTimeout(String fsTimeout) {
        runFromFileBuilder.setFsTimeout(fsTimeout);
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
     * Sets per scenario time out.
     *
     * @param perScenarioTimeOut the per scenario time out
     */
    @DataBoundSetter
    public void setPerScenarioTimeOut(String perScenarioTimeOut) {
        runFromFileBuilder.setPerScenarioTimeOut(perScenarioTimeOut);
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
     * Sets ignore error strings.
     *
     * @param ignoreErrorStrings the ignore error strings
     */
    @DataBoundSetter
    public void setIgnoreErrorStrings(String ignoreErrorStrings) {
        runFromFileBuilder.setIgnoreErrorStrings(ignoreErrorStrings);
    }

    /**
     * Gets run from file builder.
     *
     * @return the run from file builder
     */
    public RunFromFileBuilder getRunFromFileBuilder() {
        return runFromFileBuilder;
    }

    public RunResultRecorder getRunResultRecorder() {
        return runResultRecorder;
    }

    /**
     * The type Descriptor.
     */
    @Extension
    @Symbol("LrScenarioLoad")
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        /**
         * Instantiates a new Descriptor.
         */
        public DescriptorImpl() {
            super(LrScenarioLoadStepExecution.class);
        }

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
