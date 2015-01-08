package com.hp.octane.plugins.jenkins.model.pipeline.utils;

import com.hp.octane.plugins.jenkins.model.pipeline.FlowPhase;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/01/15
 * Time: 00:59
 * To change this template use File | Settings | File Templates.
 */

public abstract class AbstractProjectProcessor {
	private ArrayList<FlowPhase> internals = new ArrayList<FlowPhase>();
	private ArrayList<FlowPhase> postBuilds = new ArrayList<FlowPhase>();

	protected void processBuilders(List<Builder> builders, AbstractProject project) {
		IBuilderProcessor builderProcessor = null;
		for (Builder builder : builders) {
			if (builder.getClass().getName().compareTo("hudson.plugins.parameterizedtrigger.TriggerBuilder") == 0) {
				builderProcessor = new TriggerBuilderProcessor(builder, project);
			} else if (builder.getClass().getName().compareTo("com.tikal.jenkins.plugins.multijob.MultiJobBuilder") == 0) {
				builderProcessor = new MultiJobBuilderProcessor(builder);
			}
			if (builderProcessor != null) {
				internals.addAll(builderProcessor.getPhases());
			} else {
				System.out.println("not yet supported build action: " + builder.getClass().getName());
				//  TODO: probably we need to add the support for more stuff like:
				//      org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder
				//      org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder
			}
		}
	}

	public FlowPhase[] getInternals() {
		return internals.toArray(new FlowPhase[internals.size()]);
	}

	public FlowPhase[] getPostBuilds() {
		return postBuilds.toArray(new FlowPhase[postBuilds.size()]);
	}
}
