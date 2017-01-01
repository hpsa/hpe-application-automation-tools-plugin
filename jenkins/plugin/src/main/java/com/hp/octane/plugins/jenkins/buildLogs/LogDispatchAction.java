package com.hp.octane.plugins.jenkins.buildLogs;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Created by benmeior on 11/28/2016.
 */

public class LogDispatchAction extends Publisher {

    @DataBoundConstructor
    public LogDispatchAction() {}

    public static LogDispatchAction.DescriptorImpl descriptor() {
        return Jenkins.getInstance().getDescriptorByType(LogDispatchAction.DescriptorImpl.class);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private boolean isApplicable;

        void setIsApplicable(boolean isApplicable) {
            this.isApplicable = isApplicable;
        }

        public DescriptorImpl() {
            isApplicable = false;
        }

        public String getDisplayName() {
            return "Send build logs to HPE Octane";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return isApplicable;
        }
    }
}
