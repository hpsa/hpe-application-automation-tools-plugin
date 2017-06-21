/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.actions.cucumber;

import com.hpe.application.automation.tools.octane.Messages;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by franksha on 07/12/2016.
 */
public class CucumberTestResultsActionPublisher extends Recorder {

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
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        CucumberTestResultsAction action = new CucumberTestResultsAction(build, glob, listener);
        build.addAction(action);
        boolean resultsFound = action.copyResultsToBuildFolder();
        return resultsFound;
    }

    @Override
    public CucumberTestResultsActionPublisher.Descriptor getDescriptor() {
        return (CucumberTestResultsActionPublisher.Descriptor) super.getDescriptor();
    }

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
