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

import com.microfocus.application.automation.tools.common.Messages;
import com.microfocus.application.automation.tools.common.model.HealthAnalyzerModel;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

import static com.microfocus.application.automation.tools.Messages.CompanyName;

public class HealthAnalyzerBuilder extends Builder implements SimpleBuildStep {
    private final List<HealthAnalyzerModel> products;

    @DataBoundConstructor
    public HealthAnalyzerBuilder(List<HealthAnalyzerModel> products) {
        this.products = products;
    }

    public List<HealthAnalyzerModel> getProducts() {
        return products;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        for (HealthAnalyzerModel product : products)
            product.perform(run, workspace, launcher, listener);
    }

    @Extension
    // @Symbol - to expose this step in the snippet generator, it's name as a parameter.
    @Symbol("healthAnalyzer")
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.HealthAnalyzerBuilder_displayName(CompanyName());
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public List<HealthAnalyzerModel.HealthAnalyzerModelDescriptor> getProducts() {
            return HealthAnalyzerModel.HealthAnalyzerModelDescriptor.all();
        }
    }
}
