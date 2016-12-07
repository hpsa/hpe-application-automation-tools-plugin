package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.model.Job;

/**
 * Created by gadiel on 30/11/2016.
 *
 * Job processors factory - should be used as a 'static' class, no instantiation, only static method/s
 */

public class JobProcessorFactory {

	private JobProcessorFactory() {
	}

	public static <T extends Job> AbstractProjectProcessor<T> getFlowProcessor(T job) {
		AbstractProjectProcessor flowProcessor;

		if (job.getClass().getName().equals("hudson.model.FreeStyleProject")) {
			flowProcessor = new FreeStyleProjectProcessor(job);
		} else if (job.getClass().getName().equals("hudson.matrix.MatrixProject")) {
			flowProcessor = new MatrixProjectProcessor(job);
		} else if (job.getClass().getName().equals("hudson.maven.MavenModuleSet")) {
			flowProcessor = new MavenProjectProcessor(job);
		} else if (job.getClass().getName().equals("com.tikal.jenkins.plugins.multijob.MultiJobProject")) {
			flowProcessor = new MultiJobProjectProcessor(job);
		} else if (job.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
			flowProcessor = new WorkFlowJobProcessor(job);
		} else {
			flowProcessor = new UnsupportedProjectProcessor(job);
		}
		return flowProcessor;
	}
}
