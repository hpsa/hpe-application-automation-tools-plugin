package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.tasks.Builder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */

class FreeStyleProjectProcessor extends AbstractProjectProcessor<FreeStyleProject> {

	FreeStyleProjectProcessor(Job job) {
		super((FreeStyleProject) job);

		//  Internal phases
		//
		super.processBuilders(this.job.getBuilders(), this.job);

		//  Post build phases
		//
		super.processPublishers(this.job);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return job.getBuilders();
	}

	@Override
	public void scheduleBuild(String parametersBody) {
		throw new RuntimeException("non yet implemented");
	}
}