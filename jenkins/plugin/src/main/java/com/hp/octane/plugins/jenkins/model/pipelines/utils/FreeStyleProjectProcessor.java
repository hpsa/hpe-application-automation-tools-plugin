package com.hp.octane.plugins.jenkins.model.pipelines.utils;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */

public class FreeStyleProjectProcessor extends AbstractProjectProcessor {
	public FreeStyleProjectProcessor(AbstractProject project) {
		FreeStyleProject p = (FreeStyleProject) project;

		//  Internal phases
		//
		super.processBuilders(p.getBuilders(), p);

		//  Post build phases
		//
		super.processPublishers(p);
	}
}