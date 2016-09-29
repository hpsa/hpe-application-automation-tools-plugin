package com.hp.application.automation.tools.pipelineSteps;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import javax.inject.Inject;

/**
 * Created by kazaky on 29/09/2016.
 */
public class SseBuildExecutor extends AbstractSynchronousNonBlockingStepExecution<Void> {

    @Inject
    private transient SseBuildStep step;

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
        step.perform(build, ws, launcher, listener);
        return null;
    }

    private static final long serialVersionUID = 1L;

}
