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
	public MavenProjectProcessor(AbstractProject project) {
		MavenModuleSet p = (MavenModuleSet) project;

		//  Internal phases - pre maven phases
		//
		super.processBuilders(p.getPrebuilders(), p, "pre-maven");

		//  Internal phases - post maven phases
		//
		super.processBuilders(p.getPostbuilders(), p, "post-maven");

		//  Post build phases
		//
		super.processPublishers(p);
	}
}
