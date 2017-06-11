package com.hpe.application.automation.tools.pipelineSteps;

import com.hpe.application.automation.tools.model.SvServiceSelectionModel;
import com.hpe.application.automation.tools.run.SvUndeployBuilder;
import hudson.Extension;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

public class SvUndeployStep extends AbstractSvStep {
    private final boolean continueIfNotDeployed;
    private final SvServiceSelectionModel serviceSelection;

    @DataBoundConstructor
    public SvUndeployStep(String serverName, boolean continueIfNotDeployed, boolean force, SvServiceSelectionModel serviceSelection) {
        super(serverName, force);
        this.continueIfNotDeployed = continueIfNotDeployed;
        this.serviceSelection = serviceSelection;
    }

    @SuppressWarnings("unused")
    public boolean isContinueIfNotDeployed() {
        return continueIfNotDeployed;
    }

    public SvServiceSelectionModel getServiceSelection() {
        return serviceSelection;
    }

    @Override
    protected SimpleBuildStep getBuilder() {
        return new SvUndeployBuilder(serverName, continueIfNotDeployed, force, serviceSelection);
    }

    @Extension
    public static class DescriptorImpl extends AbstractSvStepDescriptor<SvUndeployBuilder.DescriptorImpl> {
        public DescriptorImpl() {
            super(SvExecution.class, "svUndeployStep", new SvUndeployBuilder.DescriptorImpl());
        }
    }
}
