package com.hp.octane.plugins.jenkins.model.pipeline.utils;

import com.hp.octane.plugins.jenkins.model.pipeline.StructurePhase;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;

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
	private ArrayList<StructurePhase> internals = new ArrayList<StructurePhase>();
	private ArrayList<StructurePhase> postBuilds = new ArrayList<StructurePhase>();

	protected void processBuilders(List<Builder> builders, AbstractProject project) {
		this.processBuilders(builders, project, "");
	}

	protected void processBuilders(List<Builder> builders, AbstractProject project, String phasesName) {
		AbstractBuilderProcessor builderProcessor;
		for (Builder builder : builders) {
			builderProcessor = null;
			if (builder.getClass().getName().compareTo("hudson.plugins.parameterizedtrigger.TriggerBuilder") == 0) {
				builderProcessor = new ParameterizedTriggerProcessor(builder, project, phasesName);
			} else if (builder.getClass().getName().compareTo("com.tikal.jenkins.plugins.multijob.MultiJobBuilder") == 0) {
				builderProcessor = new MultiJobBuilderProcessor(builder);
			}
			if (builderProcessor != null) {
				internals.addAll(builderProcessor.getPhases());
			} else {
				System.out.println("not yet supported build (internal) action: " + builder.getClass().getName());
				//  TODO: probably we need to add the support for more stuff like:
				//      org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder
				//      org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder
			}
		}
	}

	protected void processPublishers(AbstractProject project) {
		AbstractBuilderProcessor builderProcessor;
		List<Publisher> publishers = project.getPublishersList();
		for (Publisher publisher : publishers) {
			builderProcessor = null;
			if (publisher.getClass().getName().compareTo("hudson.tasks.BuildTrigger") == 0) {
				builderProcessor = new BuildTriggerProcessor(publisher, project);
			} else if (publisher.getClass().getName().compareTo("hudson.plugins.parameterizedtrigger.BuildTrigger") == 0) {
				builderProcessor = new ParameterizedTriggerProcessor(publisher, project, "");
			}
			if (builderProcessor != null) {
				postBuilds.addAll(builderProcessor.getPhases());
			} else {
				System.out.println("not yet supported publisher (post build) action: " + publisher.getClass().getName());
			}
		}
	}

	public StructurePhase[] getInternals() {
		return internals.toArray(new StructurePhase[internals.size()]);
	}

	public StructurePhase[] getPostBuilds() {
		return postBuilds.toArray(new StructurePhase[postBuilds.size()]);
	}
}
