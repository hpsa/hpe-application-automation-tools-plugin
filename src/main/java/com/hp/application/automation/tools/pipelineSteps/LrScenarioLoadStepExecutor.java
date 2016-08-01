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
import java.util.Map;

/**
 * Created by kazaky on 28/07/2016.
 */
public class LrScenarioLoadStepExecutor extends AbstractSynchronousNonBlockingStepExecution<Void> {

    @Inject
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

//        step.startScenarioLoad(build,ws,launcher,listener);
        RunFromFileBuilder runFromFileBuilder = new RunFromFileBuilder(step.getFsTests(), step.getFsTimeout(), step.getControllerPollingInterval(), step.getPerScenarioTimeOut(), step.getIgnoreErrorStrings(), "", "", "", "", "", "", "", "", "", "", "", "", "", null, false);
        RunResultRecorder runResultRecorder = new RunResultRecorder(step.isPublishResults(), step.getArchiveTestResultsMode());

        HashMap<String,String> resultFilename = new HashMap<String, String>(0);
        resultFilename.put(RunFromFileBuilder.class.getName(), runFromFileBuilder.getRunResultsFileName());

        runFromFileBuilder.perform(build, ws, launcher, listener);
        runResultRecorder.pipelinePerform(build, ws, launcher, listener, resultFilename);

        return null;
    }

    private static final long serialVersionUID = 1L;
}
