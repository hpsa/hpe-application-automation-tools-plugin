package com.hp.octane.plugins.jenkins.model.processors.projects;

import com.hp.nga.integrations.dto.pipelines.StructurePhase;
import com.hp.octane.plugins.jenkins.model.processors.builders.AbstractBuilderProcessor;
import com.hp.octane.plugins.jenkins.model.processors.builders.BuildTriggerProcessor;
import com.hp.octane.plugins.jenkins.model.processors.builders.MultiJobBuilderProcessor;
import com.hp.octane.plugins.jenkins.model.processors.builders.ParameterizedTriggerProcessor;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/01/15
 * Time: 00:59
 * To change this template use File | Settings | File Templates.
 */

public abstract class AbstractProjectProcessor {
	private static final Logger logger = Logger.getLogger(AbstractProjectProcessor.class.getName());

	private List<StructurePhase> internals = new ArrayList<StructurePhase>();
	private List<StructurePhase> postBuilds = new ArrayList<StructurePhase>();

	protected void processBuilders(List<Builder> builders, AbstractProject project) {
		this.processBuilders(builders, project, "");
	}

	protected void processBuilders(List<Builder> builders, AbstractProject project, String phasesName) {
		AbstractBuilderProcessor builderProcessor;
		for (Builder builder : builders) {
			builderProcessor = null;
			if (builder.getClass().getName().equals("hudson.plugins.parameterizedtrigger.TriggerBuilder")) {
				builderProcessor = new ParameterizedTriggerProcessor(builder, project, phasesName);
			} else if (builder.getClass().getName().equals("com.tikal.jenkins.plugins.multijob.MultiJobBuilder")) {
				builderProcessor = new MultiJobBuilderProcessor(builder);
			}
			if (builderProcessor != null) {
				internals.addAll(builderProcessor.getPhases());
			} else {
				logger.info("not yet supported build (internal) action: " + builder.getClass().getName());
				//  TODO: probably we need to add the support for more stuff like:
				//      org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder
				//      org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void processPublishers(AbstractProject project) {
		AbstractBuilderProcessor builderProcessor;
		List<Publisher> publishers = project.getPublishersList();
		for (Publisher publisher : publishers) {
			builderProcessor = null;
			if (publisher.getClass().getName().equals("hudson.tasks.BuildTrigger")) {
				builderProcessor = new BuildTriggerProcessor(publisher, project);
			} else if (publisher.getClass().getName().equals("hudson.plugins.parameterizedtrigger.BuildTrigger")) {
				builderProcessor = new ParameterizedTriggerProcessor(publisher, project, "");
			}
			if (builderProcessor != null) {
				postBuilds.addAll(builderProcessor.getPhases());
			} else {
				logger.info("not yet supported publisher (post build) action: " + publisher.getClass().getName());
			}
		}
	}

	public List<StructurePhase> getInternals() {
		return internals;
	}

	public List<StructurePhase> getPostBuilds() {
		return postBuilds;
	}

	public static AbstractProjectProcessor getFlowProcessor(AbstractProject project) {
		AbstractProjectProcessor flowProcessor = null;
		if (project.getClass().getName().equals("hudson.model.FreeStyleProject")) {
			flowProcessor = new FreeStyleProjectProcessor(project);
		} else if (project.getClass().getName().equals("hudson.matrix.MatrixProject")) {
			flowProcessor = new MatrixProjectProcessor(project);
		} else if (project.getClass().getName().equals("hudson.maven.MavenModuleSet")) {
			flowProcessor = new MavenProjectProcessor(project);
		} else if (project.getClass().getName().equals("com.tikal.jenkins.plugins.multijob.MultiJobProject")) {
			flowProcessor = new MultiJobProjectProcessor(project);
		} else {
			flowProcessor = new UnsupportedProjectProcessor();
		}
		return flowProcessor;
	}
}
