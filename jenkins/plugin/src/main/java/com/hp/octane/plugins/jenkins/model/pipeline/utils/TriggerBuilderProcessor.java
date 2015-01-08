package com.hp.octane.plugins.jenkins.model.pipeline.utils;

import com.hp.octane.plugins.jenkins.model.pipeline.FlowPhase;
import hudson.model.AbstractProject;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:01
 * To change this template use File | Settings | File Templates.
 */

public class TriggerBuilderProcessor implements IBuilderProcessor {
	private ArrayList<FlowPhase> phases;

	private TriggerBuilderProcessor() {
	}

	public TriggerBuilderProcessor(Builder builder, AbstractProject project) {
		TriggerBuilder b = (TriggerBuilder) builder;
		phases = new ArrayList<FlowPhase>();
		List<AbstractProject> items;
		for (BlockableBuildTriggerConfig config : b.getConfigs()) {
			items = config.getProjectList(project.getParent(), null);
			phases.add(new FlowPhase("", config.getBlock() != null, items));
		}
	}

	@Override
	public List<FlowPhase> getPhases() {
		return phases;
	}
}
