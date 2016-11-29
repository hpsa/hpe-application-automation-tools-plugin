package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */

public class MatrixProjectProcessor extends AbstractProjectProcessor {

	public MatrixProjectProcessor(AbstractProject project) {
		super(project);

		//  Internal phases
		//
		super.processBuilders(((MatrixProject)this.project).getBuilders(), this.project);

		//  Post build phases
		//
		super.processPublishers(this.project);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return ((MatrixProject)project).getBuilders();
	}
}
