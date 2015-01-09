package com.hp.octane.plugins.jenkins.model.pipeline.utils;

import com.hp.octane.plugins.jenkins.model.pipeline.FlowPhase;
import com.tikal.jenkins.plugins.multijob.MultiJobBuilder;
import com.tikal.jenkins.plugins.multijob.PhaseJobsConfig;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */

public class MultiJobBuilderProcessor extends AbstractBuilderProcessor {
	private MultiJobBuilderProcessor() {
	}

	public MultiJobBuilderProcessor(Builder builder) {
		MultiJobBuilder b = (MultiJobBuilder) builder;
		super.phases = new ArrayList<FlowPhase>();
		ArrayList<AbstractProject> items = new ArrayList<AbstractProject>();
		for (PhaseJobsConfig config : b.getPhaseJobs()) {
			items.add((AbstractProject) Jenkins.getInstance().getItem(config.getJobName()));
		}
		super.phases.add(new FlowPhase(b.getPhaseName(), true, items));
	}
}
