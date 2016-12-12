package com.hp.octane.plugins.jenkins.model.processors.projects;

import com.hp.octane.integrations.dto.api.pipelines.PipelinePhase;
import com.hp.octane.plugins.jenkins.model.processors.builders.AbstractBuilderProcessor;
import com.hp.octane.plugins.jenkins.model.processors.builders.BuildTriggerProcessor;
import com.hp.octane.plugins.jenkins.model.processors.builders.MultiJobBuilderProcessor;
import com.hp.octane.plugins.jenkins.model.processors.builders.ParameterizedTriggerProcessor;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.tasks.BuildStep;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder;
import org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/01/15
 * Time: 00:59
 * To change this template use File | Settings | File Templates.
 */

public abstract class AbstractProjectProcessor<T extends Job> {
	private static final Logger logger = LogManager.getLogger(AbstractProjectProcessor.class);
	private final List<PipelinePhase> internals = new ArrayList<>();
	private final List<PipelinePhase> postBuilds = new ArrayList<>();

	T job;

	AbstractProjectProcessor(T job) {
		this.job = job;
	}

	//  PUBLIC APIs
	//
	public abstract List<Builder> tryGetBuilders();

	public abstract void scheduleBuild(String parametersBody);

	public String getJobCiId() {
		if (job.getParent().getClass().getName().equals("com.cloudbees.hudson.plugins.folder.Folder")) {
			String jobPlainName = job.getFullName();    // e.g: myFolder/myJob
			return jobPlainName.replaceAll("/", "/job/");
		} else {
			return job.getName();
		}
	}

	public List<PipelinePhase> getInternals() {
		return internals;
	}

	public List<PipelinePhase> getPostBuilds() {
		return postBuilds;
	}

	//  INTERNALS
	//
	void processBuilders(List<Builder> builders, Job job) {
		this.processBuilders(builders, job, "");
	}

	void processBuilders(List<Builder> builders, Job job, String phasesName) {
		for (Builder builder : builders) {
			builderClassValidator(builder, job, phasesName);
		}
	}

	@SuppressWarnings("unchecked")
	void processPublishers(Job job) {
		if (job instanceof AbstractProject) {
			AbstractProject project = (AbstractProject) job;
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
	}

	private void builderClassValidator(Builder builder, Job job, String phasesName) {
		AbstractBuilderProcessor builderProcessor = null;
		if (builder.getClass().getName().equals("org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder")) {
			ConditionalBuilder conditionalBuilder = (ConditionalBuilder) builder;
			for (BuildStep currentBuildStep : conditionalBuilder.getConditionalbuilders()) {
				builderClassValidator((Builder) currentBuildStep, job, phasesName);
			}
		} else if (builder.getClass().getName().equals("org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder")) {
			SingleConditionalBuilder singleConditionalBuilder = (SingleConditionalBuilder) builder;
			builderClassValidator((Builder) singleConditionalBuilder.getBuildStep(), job, phasesName);
		} else if (builder.getClass().getName().equals("hudson.plugins.parameterizedtrigger.TriggerBuilder")) {
			builderProcessor = new ParameterizedTriggerProcessor(builder, job, phasesName);
		} else if (builder.getClass().getName().equals("com.tikal.jenkins.plugins.multijob.MultiJobBuilder")) {
			builderProcessor = new MultiJobBuilderProcessor(builder);
		}

		if (builderProcessor != null) {
			internals.addAll(builderProcessor.getPhases());
		} else {
			logger.info("not yet supported build (internal) action: " + builder.getClass().getName());
		}
	}
}
