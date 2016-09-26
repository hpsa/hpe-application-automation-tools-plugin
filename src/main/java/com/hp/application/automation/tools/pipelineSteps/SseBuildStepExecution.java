package com.hp.application.automation.tools.pipelineSteps;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

public class SseBuildStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {
	
	private static final long serialVersionUID = 1L;

	@Inject
	private transient SseBuildStep step;
	
	@StepContextParameter
	private transient BuildListener listener;
	
	@StepContextParameter
	private transient AbstractBuild<?, ?> build;
	
	@StepContextParameter
	private transient Launcher launcher;

	@Override
	protected Void run() throws Exception {
		step.getSseBuilder().perform(build, launcher, listener);
		return null;
	}

}
