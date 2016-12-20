package com.hp.application.automation.tools.pipelineSteps;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/** 
* LoadRunnerTestStep Tester.
* 
* @author <Authors name> 
* @since <pre>??? 28, 2016</pre> 
* @version 1.0 
*/ 
public class LrScenarioLoadStepTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();


    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: runLoadRunnerScenario
     */
    @Test
    public void testLrScenarioRun() throws Exception {
        // Setup the job
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "foo");
        job.setDefinition(new CpsFlowDefinition("node {}")); // insert here the pipeline code to run

        //run the build nad wait for successful result
        WorkflowRun workflowRun = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0).get());

        //now we need to check the results.

    }
}