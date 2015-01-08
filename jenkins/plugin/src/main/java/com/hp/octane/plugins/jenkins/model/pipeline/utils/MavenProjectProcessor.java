package com.hp.octane.plugins.jenkins.model.pipeline.utils;

import hudson.maven.MavenModuleSet;
import hudson.model.AbstractProject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:35
 * To change this template use File | Settings | File Templates.
 */

public class MavenProjectProcessor extends AbstractProjectProcessor{
	private MavenProjectProcessor() {
	}

	public MavenProjectProcessor(AbstractProject project) {
		if (project == null) throw new IllegalArgumentException("project MUST not be null");
		MavenModuleSet p = (MavenModuleSet) project;

		//  Internal phases - pre maven phases
		//
		super.processBuilders(p.getPrebuilders(), p);

		//  Internal phases - post maven phases
		//
		super.processBuilders(p.getPostbuilders(), p);

		//  Post build phases
		//
		//  TODO: add processing publishers here
	}
}
