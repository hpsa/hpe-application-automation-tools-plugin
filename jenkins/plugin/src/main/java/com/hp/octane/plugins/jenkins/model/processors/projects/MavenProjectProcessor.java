package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.maven.MavenModuleSet;
import hudson.model.AbstractProject;
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

public class MavenProjectProcessor extends AbstractProjectProcessor{


	public MavenProjectProcessor(AbstractProject project) {
		super(project);
		//  Internal phases - pre maven phases
		//
		super.processBuilders(((MavenModuleSet)this.project).getPrebuilders(), this.project, "pre-maven");

		//  Internal phases - post maven phases
		//
		super.processBuilders(((MavenModuleSet)this.project).getPostbuilders(), this.project, "post-maven");

		//  Post build phases
		//
		super.processPublishers(this.project);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return new ArrayList<Builder>();
	}
}
