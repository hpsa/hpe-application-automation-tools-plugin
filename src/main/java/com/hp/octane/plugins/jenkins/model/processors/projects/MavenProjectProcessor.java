package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.maven.MavenModuleSet;
import hudson.model.Job;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:35
 * To change this template use File | Settings | File Templates.
 */

class MavenProjectProcessor extends AbstractProjectProcessor<MavenModuleSet> {

	MavenProjectProcessor(Job mavenJob) {
		super((MavenModuleSet) mavenJob);
		//  Internal phases - pre maven phases
		//
		super.processBuilders(this.job.getPrebuilders(), this.job, "pre-maven");

		//  Internal phases - post maven phases
		//
		super.processBuilders(this.job.getPostbuilders(), this.job, "post-maven");

		//  Post build phases
		//
		super.processPublishers(this.job);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return new ArrayList<>();
	}

	@Override
	public void scheduleBuild(String parametersBody) {
		throw new RuntimeException("non yet implemented");
	}
}
