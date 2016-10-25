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
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * UFT pipeline step
 */
public class UftScenarioLoadStep extends AbstractStepImpl {

    private final RunFromFileBuilder runFromFileBuilder;
    private final RunResultRecorder runResultRecorder;

    /**
     * Instantiates a new UFT scenario load step.
     *
     * @param testPaths              the test paths
     * @param archiveTestResultsMode the type of archiving the user wants.
     */
    @DataBoundConstructor
    public UftScenarioLoadStep(String testPaths, String archiveTestResultsMode) {
        this.runFromFileBuilder = new RunFromFileBuilder(testPaths);
        this.runResultRecorder = new RunResultRecorder(archiveTestResultsMode);
    }

    public String getArchiveTestResultsMode() {
        return runResultRecorder.getResultsPublisherModel().getArchiveTestResultsMode();
    }

    public String getFsTimeout() {
        return runFromFileBuilder.getRunFromFileModel().getFsTimeout();
    }

    @DataBoundSetter
    public void setFsTimeout(String fsTimeout) {
        runFromFileBuilder.setFsTimeout(fsTimeout);
    }

    public String getTestPaths() {
        return runFromFileBuilder.getRunFromFileModel().getFsTests();
    }

    public RunFromFileBuilder getRunFromFileBuilder() {
        return runFromFileBuilder;
    }

    public RunResultRecorder getRunResultRecorder() {
        return runResultRecorder;
    }

    @Extension
    @Symbol("UftScenarioLoad")
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(UftScenarioLoadStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "uftScenarioLoad";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Run UFT scenario";
        }

        public List<EnumDescription> getReportArchiveModes() {

            return ResultsPublisherModel.archiveModes;
        }

        public FormValidation doCheckTestPaths(@QueryParameter String value) {

            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Test path must be set");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckFsTests(@QueryParameter String value) {

            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Test path must be set");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckFsTimeout(@QueryParameter String value) {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.ok();
            }

            String val1 = value.trim();
            if (val1.length() > 0 && val1.charAt(0) == '-') {
                val1 = val1.substring(1);
            }

            if (!StringUtils.isNumeric(val1) && !Objects.equals(val1, "")) {
                return FormValidation.error("Timeout name must be a number");
            }

            return FormValidation.ok();
        }

    }

}
