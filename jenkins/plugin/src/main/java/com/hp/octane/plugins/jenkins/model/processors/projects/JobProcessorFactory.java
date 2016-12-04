package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.model.AbstractProject;
import hudson.model.Job;

/**
 * Created by gadiel on 30/11/2016.
 */
public class JobProcessorFactory {

    public static AbstractProjectProcessor getFlowProcessor(Job job) {
        AbstractProjectProcessor flowProcessor = null;

        if (job instanceof AbstractProject) {
            if (job.getClass().getName().equals("hudson.model.FreeStyleProject")) {
                flowProcessor = new FreeStyleProjectProcessor(job);
            } else if (job.getClass().getName().equals("hudson.matrix.MatrixProject")) {
                flowProcessor = new MatrixProjectProcessor(job);
            } else if (job.getClass().getName().equals("hudson.maven.MavenModuleSet")) {
                flowProcessor = new MavenProjectProcessor(job);
            } else if (job.getClass().getName().equals("com.tikal.jenkins.plugins.multijob.MultiJobProject")) {
                flowProcessor = new MultiJobProjectProcessor(job);
            } else {
                flowProcessor = new UnsupportedProjectProcessor(job);
            }
        } else if (job.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
            flowProcessor = new WorkFlowJobProcessor(job);
        }

        return flowProcessor;
    }
}
