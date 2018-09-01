/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.common.run;

import com.microfocus.application.automation.tools.common.model.HealthAnalyzerModel;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class HealthAnalyzerBuilder extends Builder implements SimpleBuildStep {
    private final HealthAnalyzerModel healthAnalyzerModel;

    @DataBoundConstructor
    public HealthAnalyzerBuilder(HealthAnalyzerModel healthAnalyzerModel) {
        this.healthAnalyzerModel = healthAnalyzerModel;
    }

    public HealthAnalyzerModel getHealthAnalyzerModel() {
        return healthAnalyzerModel;
    }

    @Override
    public void perform
            (@Nonnull Run<?, ?> run, @Nonnull FilePath workspace,
             @Nonnull Launcher launcher, @Nonnull TaskListener listener)
            throws InterruptedException, IOException {
        healthAnalyzerModel.perform(run, workspace, launcher, listener);
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Micro Focus Health Analyzer Builder";
        }

        public DescriptorExtensionList
                <HealthAnalyzerModel, HealthAnalyzerModel.HealthAnalyzerModelDescriptor> getHealthAnalyzerDescriptors() {
            return HealthAnalyzerModel.all();
        }
    }
}
