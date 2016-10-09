package com.hp.application.automation.tools.pipelineSteps;

import com.hp.application.automation.tools.results.RunResultRecorder;
import com.hp.application.automation.tools.run.RunFromFileBuilder;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import javax.inject.Inject;
import java.util.HashMap;


/**
 * The Load runner pipeline step execution.
 */
public class LrScenarioLoadStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

  private static final long serialVersionUID = 1L;
  @Inject
  @SuppressWarnings("squid:S3306")
  private transient LrScenarioLoadStep step;
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
    listener.getLogger().println("Running LoadRunner Scenario step");

    RunResultRecorder runResultRecorder = new RunResultRecorder(step.isPublishResults(), step.getArchiveRunTestResultsMode());
    step.getRunFromFileBuilder().perform(build, ws, launcher, listener);

    HashMap<String, String> resultFilename = new HashMap<String, String>(0);
    resultFilename.put(RunFromFileBuilder.class.getName(), step.getRunFromFileBuilder().getRunResultsFileName());

    runResultRecorder.pipelinePerform(build, ws, launcher, listener, resultFilename);

    return null;
  }
}
