package com.hp.octane.plugins.jenkins.model.pipeline.utils;

import com.tikal.jenkins.plugins.multijob.MultiJobProject;
import hudson.model.AbstractProject;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */

public class MultiJobProjectProcessor extends AbstractProjectProcessor {
	public MultiJobProjectProcessor(AbstractProject project) {
		MultiJobProject p = (MultiJobProject) project;

		//  Internal phases
		//
		super.processBuilders(p.getBuilders(), p);

		//  Post build phases
		//
		super.processPublishers(p);
	}
}
