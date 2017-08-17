package com.hpe.application.automation.tools.pipelineSteps;

import com.hpe.application.automation.tools.model.SvDataModelSelection;
import com.hpe.application.automation.tools.model.SvPerformanceModelSelection;
import com.hpe.application.automation.tools.model.SvServiceSelectionModel;
import com.hpe.application.automation.tools.run.SvChangeModeBuilder;
import com.hp.sv.jsvconfigurator.core.impl.jaxb.ServiceRuntimeConfiguration;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class SvChangeModeStep extends AbstractSvStep {
    private final ServiceRuntimeConfiguration.RuntimeMode mode;
    private final SvDataModelSelection dataModel;
    private final SvPerformanceModelSelection performanceModel;
    private final SvServiceSelectionModel serviceSelection;

    @DataBoundConstructor
    public SvChangeModeStep(String serverName, boolean force, ServiceRuntimeConfiguration.RuntimeMode mode,
                            SvDataModelSelection dataModel, SvPerformanceModelSelection performanceModel, SvServiceSelectionModel serviceSelection) {
        super(serverName, force);
        this.mode = mode;
        this.dataModel = dataModel;
        this.performanceModel = performanceModel;
        this.serviceSelection = serviceSelection;
    }

    public ServiceRuntimeConfiguration.RuntimeMode getMode() {
        return mode;
    }

    public SvDataModelSelection getDataModel() {
        return dataModel;
    }

    public SvPerformanceModelSelection getPerformanceModel() {
        return performanceModel;
    }

    public SvServiceSelectionModel getServiceSelection() {
        return serviceSelection;
    }

    @Override
    protected SimpleBuildStep getBuilder() {
        return new SvChangeModeBuilder(serverName, force, mode, dataModel, performanceModel, serviceSelection);
    }

    @Extension
    public static class DescriptorImpl extends AbstractSvStepDescriptor<SvChangeModeBuilder.DescriptorImpl> {
        public DescriptorImpl() {
            super(SvExecution.class, "svChangeModeStep", new SvChangeModeBuilder.DescriptorImpl());
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckDataModel(@QueryParameter String value, @QueryParameter("mode") String mode,
                                               @QueryParameter("serviceSelectionKind") String kind) {
            return builderDescriptor.doCheckDataModel(value, mode, kind);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillModeItems() {
            return builderDescriptor.doFillModeItems();
        }
    }
}
