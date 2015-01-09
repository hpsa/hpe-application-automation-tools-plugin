package com.hp.octane.plugins.jenkins.model.pipeline.utils;

import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */

public class MatrixProjectProcessor extends AbstractProjectProcessor {
	private MatrixProjectProcessor() {
	}

	public MatrixProjectProcessor(AbstractProject project) {
		if (project == null) throw new IllegalArgumentException("project MUST not be null");
		MatrixProject p = (MatrixProject) project;

		//  Internal phases
		//
		super.processBuilders(p.getBuilders(), p);

		//  Post build phases
		//
		super.processPublishers(p);
	}
}
