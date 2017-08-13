package com.hpe.application.automation.tools.pipelineSteps;

import javax.inject.Inject;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

public class SvExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private transient AbstractSvStep step;
    @StepContextParameter
    private transient TaskListener listener;
    @StepContextParameter
    private transient FilePath ws;
    @StepContextParameter
    private transient Run build;
    @StepContextParameter
    private transient Launcher launcher;

    @Override
    protected Void run() throws Exception {
        step.getBuilder().perform(build, ws, launcher, listener);
        return null;
    }
}
