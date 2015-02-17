package com.hp.octane.plugins.jenkins.model.pipelines.utils;

import com.hp.octane.plugins.jenkins.model.pipelines.StructurePhase;
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
	public ParameterizedTriggerProcessor(Builder builder, AbstractProject project, String phasesName) {
		TriggerBuilder b = (TriggerBuilder) builder;
		super.phases = new ArrayList<StructurePhase>();
		List<AbstractProject> items;
		for (BlockableBuildTriggerConfig config : b.getConfigs()) {
			items = config.getProjectList(project.getParent(), null);
			super.phases.add(new StructurePhase(phasesName, config.getBlock() != null, items));
		}
	}

	public ParameterizedTriggerProcessor(Publisher publisher, AbstractProject project, String phasesName) {
		BuildTrigger t = (BuildTrigger) publisher;
		super.phases = new ArrayList<StructurePhase>();
		List<AbstractProject> items;
		for (BuildTriggerConfig config : t.getConfigs()) {
			items = config.getProjectList(project.getParent(), null);
			super.phases.add(new StructurePhase(phasesName, false, items));
		}
	}
}
