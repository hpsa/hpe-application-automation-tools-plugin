package com.hpe.application.automation.tools.pipelineSteps;

import com.hpe.application.automation.tools.run.SvDeployBuilder;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class SvDeployStep extends AbstractSvStep {
    private final String service;
    private final String projectPath;
    private final String projectPassword;
    private final boolean firstAgentFallback;

    @DataBoundConstructor
    public SvDeployStep(String serverName, boolean force, String service, String projectPath, String projectPassword, boolean firstAgentFallback) {
        super(serverName, force);
        this.service = service;
        this.projectPath = projectPath;
        this.projectPassword = projectPassword;
        this.firstAgentFallback = firstAgentFallback;
    }

    public String getService() {
        return service;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getProjectPassword() {
        return projectPassword;
    }

    public boolean isFirstAgentFallback() {
        return firstAgentFallback;
    }

    @Override
    protected SimpleBuildStep getBuilder() {
        return new SvDeployBuilder(serverName, force, service, projectPath, projectPassword, firstAgentFallback);
    }

    @Extension
    public static class DescriptorImpl extends AbstractSvStepDescriptor<SvDeployBuilder.DescriptorImpl> {
        public DescriptorImpl() {
            super(SvExecution.class, "svDeployStep", new SvDeployBuilder.DescriptorImpl());
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckProjectPath(@QueryParameter String projectPath) {
            return builderDescriptor.doCheckProjectPath(projectPath);
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckService(@QueryParameter String service) {
            return builderDescriptor.doCheckService(service);
        }
    }
}
