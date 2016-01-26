package com.hp.octane.plugins.jenkins.model.processors.builders;

import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.pipelines.StructurePhase;
import com.hp.octane.plugins.jenkins.model.processors.projects.AbstractProjectProcessor;
import hudson.model.AbstractProject;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:01
 * To change this template use File | Settings | File Templates.
 */

public class ParameterizedTriggerProcessor extends AbstractBuilderProcessor {
	private static final Logger logger = Logger.getLogger(ParameterizedTriggerProcessor.class.getName());

	public ParameterizedTriggerProcessor(Builder builder, AbstractProject project, String phasesName) {
		TriggerBuilder b = (TriggerBuilder) builder;
		super.phases = new ArrayList<StructurePhase>();
		List<AbstractProject> items;
		for (BlockableBuildTriggerConfig config : b.getConfigs()) {
			items = config.getProjectList(project.getParent(), null);
			for (Iterator<AbstractProject> iterator = items.iterator(); iterator.hasNext(); ) {
				AbstractProject next = iterator.next();
				if (next == null) {
					iterator.remove();
					logger.severe("encountered null project reference; considering it as corrupted configuration and skipping");
				}
			}
			StructurePhase newPhase = new StructurePhase();
			newPhase.setName(phasesName);
			newPhase.setBlocking(config.getBlock() != null);
			newPhase.setJobs(createSubJobs(items));
			super.phases.add(newPhase);
		}
	}

	public ParameterizedTriggerProcessor(Publisher publisher, AbstractProject project, String phasesName) {
		BuildTrigger t = (BuildTrigger) publisher;
		super.phases = new ArrayList<StructurePhase>();
		List<AbstractProject> items;
		for (BuildTriggerConfig config : t.getConfigs()) {
			items = config.getProjectList(project.getParent(), null);
			for (Iterator<AbstractProject> iterator = items.iterator(); iterator.hasNext(); ) {
				AbstractProject next = iterator.next();
				if (next == null) {
					iterator.remove();
					logger.severe("encountered null project reference; considering it as corrupted configuration and skipping");
				}
			}
			StructurePhase newPhase = new StructurePhase();
			newPhase.setName(phasesName);
			newPhase.setBlocking(false);
			newPhase.setJobs(createSubJobs(items));
			super.phases.add(newPhase);
		}
	}


	private List<StructureItem> createSubJobs(List<AbstractProject> projects) {
		List<StructureItem> result = new ArrayList<StructureItem>();
		StructureItem tmp;
		AbstractProjectProcessor projectProcessor;
		for (AbstractProject project : projects) {
			if (project != null) {
				tmp = new StructureItem();
				tmp.setName(project.getName());
				//  TODO: parameters
				projectProcessor = AbstractProjectProcessor.getFlowProcessor(project);
				tmp.setInternals(Arrays.asList(projectProcessor.getInternals()));
				tmp.setPostBuilds(Arrays.asList(projectProcessor.getPostBuilds()));
				result.add(tmp);
			}
		}
		return result;
	}
}
