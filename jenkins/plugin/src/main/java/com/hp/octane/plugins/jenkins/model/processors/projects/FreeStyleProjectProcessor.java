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

public class FreeStyleProjectProcessor extends AbstractProjectProcessor {

	public FreeStyleProjectProcessor(Job job) {
		super(job);

		//  Internal phases
		//
		super.processBuilders(((FreeStyleProject) this.job).getBuilders(), this.job);

		//  Post build phases
		//
		super.processPublishers(this.job);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return ((FreeStyleProject)job).getBuilders();
	}

}