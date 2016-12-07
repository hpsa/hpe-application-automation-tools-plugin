package com.hp.octane.plugins.jenkins.model.processors.projects;

import com.tikal.jenkins.plugins.multijob.MultiJobProject;
import hudson.model.Job;
import hudson.tasks.Builder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */

class MultiJobProjectProcessor extends AbstractProjectProcessor<MultiJobProject> {

	MultiJobProjectProcessor(Job job) {
		super((MultiJobProject) job);
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
