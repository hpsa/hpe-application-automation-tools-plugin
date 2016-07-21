package com.hp.octane.plugins.jenkins.workflow;

import com.hp.octane.plugins.jenkins.model.processors.projects.AbstractProjectProcessor;
import hudson.model.Job;
import hudson.tasks.Builder;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */

public class WorkFlowJobProcessor extends AbstractProjectProcessor {
	WorkflowJob workflowJob;

	public WorkFlowJobProcessor(Job project) {
		this.workflowJob = (WorkflowJob) project;

		//  Internal phases
		//
		//super.processBuilders(p.getBuilders(), p);

		//  Post build phases
		//
		//super.processPublishers(p);


	}

	public List<Builder> tryGetBuilders() {
		return new ArrayList<Builder>();
	}


}
