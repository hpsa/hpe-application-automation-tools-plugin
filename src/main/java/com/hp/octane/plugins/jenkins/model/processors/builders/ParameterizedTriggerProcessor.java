package com.hp.octane.plugins.jenkins.model.processors.builders;

import com.hp.octane.plugins.jenkins.model.ModelFactory;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:01
 * To change this template use File | Settings | File Templates.
 */

public class ParameterizedTriggerProcessor extends AbstractBuilderProcessor {
	private static final Logger logger = LogManager.getLogger(ParameterizedTriggerProcessor.class);

	public ParameterizedTriggerProcessor(Builder builder, Job job, String phasesName) {
		TriggerBuilder b = (TriggerBuilder) builder;
		super.phases = new ArrayList<>();
		List<AbstractProject> items;
		for (BlockableBuildTriggerConfig config : b.getConfigs()) {
			items = config.getProjectList(job.getParent(), null);
			for (Iterator<AbstractProject> iterator = items.iterator(); iterator.hasNext(); ) {
				AbstractProject next = iterator.next();
				if (next == null) {
					iterator.remove();
					logger.warn("encountered null project reference; considering it as corrupted configuration and skipping");
				}
			}
			super.phases.add(ModelFactory.createStructurePhase(phasesName, config.getBlock() != null, items));
		}
	}

	public ParameterizedTriggerProcessor(Publisher publisher, AbstractProject project, String phasesName) {
		BuildTrigger t = (BuildTrigger) publisher;
		super.phases = new ArrayList<>();
		List<AbstractProject> items;
		for (BuildTriggerConfig config : t.getConfigs()) {
			items = config.getProjectList(project.getParent(), null);
			for (Iterator<AbstractProject> iterator = items.iterator(); iterator.hasNext(); ) {
				AbstractProject next = iterator.next();
				if (next == null) {
					iterator.remove();
					logger.warn("encountered null project reference; considering it as corrupted configuration and skipping");
				}
			}
			super.phases.add(ModelFactory.createStructurePhase(phasesName, false, items));
		}
	}
}
