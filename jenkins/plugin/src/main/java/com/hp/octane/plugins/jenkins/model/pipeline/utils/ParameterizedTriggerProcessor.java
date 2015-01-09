package com.hp.octane.plugins.jenkins.model.pipeline.utils;

import com.hp.octane.plugins.jenkins.model.pipeline.FlowPhase;
import hudson.model.AbstractProject;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:01
 * To change this template use File | Settings | File Templates.
 */

public class ParameterizedTriggerProcessor extends AbstractBuilderProcessor {
	private ParameterizedTriggerProcessor() {
	}

	public ParameterizedTriggerProcessor(Builder builder, AbstractProject project) {
		TriggerBuilder b = (TriggerBuilder) builder;
		super.phases = new ArrayList<FlowPhase>();
		List<AbstractProject> items;
		for (BlockableBuildTriggerConfig config : b.getConfigs()) {
			items = config.getProjectList(project.getParent(), null);
			super.phases.add(new FlowPhase("", config.getBlock() != null, items));
		}
	}

	public ParameterizedTriggerProcessor(Publisher publisher, AbstractProject project) {
		BuildTrigger t = (BuildTrigger) publisher;
		super.phases = new ArrayList<FlowPhase>();
		List<AbstractProject> items;
		for (BuildTriggerConfig config : t.getConfigs()) {
			items = config.getProjectList(project.getParent(), null);
			super.phases.add(new FlowPhase("", false, items));
		}
	}
}
