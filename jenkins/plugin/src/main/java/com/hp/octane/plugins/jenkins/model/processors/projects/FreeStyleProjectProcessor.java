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

	public FreeStyleProjectProcessor(AbstractProject project) {
		super(project);

		//  Internal phases
		//
		super.processBuilders(((FreeStyleProject) this.project).getBuilders(), this.project);

		//  Post build phases
		//
		super.processPublishers(this.project);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return ((FreeStyleProject)project).getBuilders();
	}

}