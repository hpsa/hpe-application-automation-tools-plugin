package com.hpe.application.automation.tools.pipelineSteps;

import com.hpe.application.automation.tools.model.SvServiceSelectionModel;
import com.hpe.application.automation.tools.run.SvExportBuilder;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class SvExportStep extends AbstractSvStep {
    private final String targetDirectory;
    private final boolean cleanTargetDirectory;
    private final SvServiceSelectionModel serviceSelection;
    private final boolean switchToStandByFirst;

    @DataBoundConstructor
    public SvExportStep(String serverName, boolean force, String targetDirectory, boolean cleanTargetDirectory,
                        SvServiceSelectionModel serviceSelection, boolean switchToStandByFirst) {
        super(serverName, force);
        this.targetDirectory = targetDirectory;
        this.cleanTargetDirectory = cleanTargetDirectory;
        this.serviceSelection = serviceSelection;
        this.switchToStandByFirst = switchToStandByFirst;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public boolean isCleanTargetDirectory() {
        return cleanTargetDirectory;
    }

    public SvServiceSelectionModel getServiceSelection() {
        return serviceSelection;
    }

    public boolean isSwitchToStandByFirst() {
        return switchToStandByFirst;
    }

    @Override
    protected SimpleBuildStep getBuilder() {
        return new SvExportBuilder(serverName, force, targetDirectory, cleanTargetDirectory, serviceSelection, switchToStandByFirst);
    }

    @Extension
    public static class DescriptorImpl extends AbstractSvStepDescriptor<SvExportBuilder.DescriptorImpl> {
        public DescriptorImpl() {
            super(SvExecution.class, "svExportStep", new SvExportBuilder.DescriptorImpl());
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckTargetDirectory(@QueryParameter String targetDirectory) {
            return builderDescriptor.doCheckTargetDirectory(targetDirectory);
        }
    }
}
