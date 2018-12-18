/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.octane.actions.cucumber;

import com.microfocus.application.automation.tools.octane.Messages;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by franksha on 07/12/2016.
 */
public class CucumberTestResultsActionPublisher extends Recorder implements SimpleBuildStep {

    private final String glob;

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundConstructor
    public CucumberTestResultsActionPublisher(String cucumberResultsGlob) {
        this.glob = cucumberResultsGlob;
    }

    public String getCucumberResultsGlob() {
        return glob;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        CucumberTestResultsAction action = new CucumberTestResultsAction(run, workspace, glob, taskListener);
        run.addAction(action);
        boolean isSuccessful = action.copyResultsToBuildFolder();
        if (!isSuccessful) {
            run.setResult(Result.FAILURE);
        }
    }

    @Override
    public CucumberTestResultsActionPublisher.Descriptor getDescriptor() {
        return (CucumberTestResultsActionPublisher.Descriptor) super.getDescriptor();
    }


    @Symbol("publishGherkinResults")
    @Extension
    public static final class Descriptor extends BuildStepDescriptor<Publisher> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return Messages.CucumberReporterName();
        }

        public FormValidation doCheckCucumberResultsGlob(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException, ServletException {
            if (value == null || value.isEmpty()) {
                return FormValidation.warning(Messages.CucumberResultsActionEmptyConfigurationWarning(), CucumberResultsService.DEFAULT_GLOB);
            } else if (project == null) {
                return FormValidation.ok();
            }
            return FilePath.validateFileMask(project.getSomeWorkspace(), value);
        }
    }
}
