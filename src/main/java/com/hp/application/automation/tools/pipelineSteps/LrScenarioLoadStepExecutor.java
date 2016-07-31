package com.hp.application.automation.tools.pipelineSteps;


import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

/**
 * Created by kazaky on 28/07/2016.
 */
public class LrScenarioLoadStepExecutor extends AbstractSynchronousNonBlockingStepExecution {

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
        System.out.println("Running LoadRunner Scenario step");

//        RunFromFileBuilder runFromFileBuilder = new RunFromFileBuilder();
//        RunResultRecorder runResultRecorder = new RunResultRecorder(true,);

        return null;
    }
}
