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

package com.microfocus.application.automation.tools.model;

import com.microfocus.application.automation.tools.sse.common.StringUtils;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.microfocus.application.automation.tools.model.RunFromFileSystemModel.isMtbxContent;

/**
 * Represents the tests that ParallelRunner should run and the environments on which to run them.
 */
public class FileSystemTestModel extends AbstractDescribableImpl<FileSystemTestModel> {
    private String tests; // tests to run
    private List<ParallelRunnerEnvironmentModel> parallelRunnerEnvironments; // the environments to run on

    /**
     * Constructs a new FileSystemTestModel
     * @param tests the tests to be run
     * @param parallelRunnerEnvironments the environments on which to run the tests on
     */
    @DataBoundConstructor
    public FileSystemTestModel(String tests,List<ParallelRunnerEnvironmentModel> parallelRunnerEnvironments) {
        this.tests = tests;
        this.parallelRunnerEnvironments = parallelRunnerEnvironments;
    }

    /**
     * Returns the tests.
     * @return the tests
     */
    public String getTests() {
        if(this.tests == null) {
            this.tests = "\n";
        }
        else if(!this.tests.contains("\n")) {
            this.tests+="\n";
        }

        return tests;
    }

    /**
     * Adds a new line to the tests string if not present (keep the expandableTextBox expanded).
     * @param tests the tests
     */
    private void setTestsWithNewLine(String tests) {
        this.tests = tests.trim();

        if (!this.tests.contains("\n")) {
            this.tests += "\n";
        }
    }

    /**
     * Set the tests to be run by ParallelRunner.
     * @param tests the tests to be run
     */
    @DataBoundSetter
    public void setTests(String tests) {
        setTestsWithNewLine(tests);
    }

    /**
     * Returns the environments on which the tests will run.
     * @return the parallel runner environments
     */
    public List<ParallelRunnerEnvironmentModel> getParallelRunnerEnvironments() {
        return parallelRunnerEnvironments;
    }

    /**
     * Sets the ParallelRunner environments.
     * @param parallelRunnerEnvironments the parallel runner environments
     */
    @DataBoundSetter
    public void setParallelRunnerEnvironments(List<ParallelRunnerEnvironmentModel> parallelRunnerEnvironments) {
        this.parallelRunnerEnvironments = parallelRunnerEnvironments;
    }

    /**
     * Expand the tests based on the environment variables.
     * @param envVars the environment variables
     * @return the parsed list of tests
     */
    public List<String> parseTests(EnvVars envVars) {
        String expandedFsTests = envVars.expand(this.tests);

        List<String> result = new ArrayList<>();

        if(StringUtils.isNullOrEmpty(this.tests))
            return result;

        if (isMtbxContent(expandedFsTests)) {
            result.add(expandedFsTests);
        } else {
            result = Arrays.asList(expandedFsTests.replaceAll("\r", "").split("\n"));
        }

        return result;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<FileSystemTestModel> {
        @Nonnull
        public String getDisplayName() {return "File System test model";}
    }
}
