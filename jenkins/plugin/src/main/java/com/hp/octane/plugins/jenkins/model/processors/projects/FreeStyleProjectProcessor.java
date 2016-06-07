package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
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
	private final FreeStyleProject project;

	public FreeStyleProjectProcessor(AbstractProject project) {
		this.project = (FreeStyleProject) project;

		//  Internal phases
		//
		super.processBuilders(this.project.getBuilders(), this.project);

		//  Post build phases
		//
		super.processPublishers(this.project);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return project.getBuilders();
	}
}