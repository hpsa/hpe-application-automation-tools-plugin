package com.hp.octane.plugins.jenkins.model.processors.projects;

import hudson.maven.MavenModuleSet;
import hudson.model.Job;
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


	public MavenProjectProcessor(Job job) {
		super(job);
		//  Internal phases - pre maven phases
		//
		super.processBuilders(((MavenModuleSet)this.job).getPrebuilders(),(MavenModuleSet) this.job, "pre-maven");

		//  Internal phases - post maven phases
		//
		super.processBuilders(((MavenModuleSet)this.job).getPostbuilders(), (MavenModuleSet)this.job, "post-maven");

		//  Post build phases
		//
		super.processPublishers((MavenModuleSet)this.job);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return new ArrayList<Builder>();
	}
}
