package com.hp.application.automation.tools.pipelineSteps;


import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;

/**
 * Created by kazaky on 28/07/2016.
 */
public class LrScenarioLoadStepExecutor extends AbstractSynchronousNonBlockingStepExecution {
    @Override
    protected Void run() throws Exception {
        System.out.println("Running LoadRunner Scenario step");
        return null;
    }
}
