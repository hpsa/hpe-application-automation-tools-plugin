package com.hp.octane.plugins.jenkins.model.processors.builders;

import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.pipelines.StructurePhase;
import com.hp.octane.plugins.jenkins.model.processors.projects.AbstractProjectProcessor;
import com.tikal.jenkins.plugins.multijob.MultiJobBuilder;
import com.tikal.jenkins.plugins.multijob.PhaseJobsConfig;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */

public class MultiJobBuilderProcessor extends AbstractBuilderProcessor {
	private static final Logger logger = Logger.getLogger(MultiJobBuilderProcessor.class.getName());

	public MultiJobBuilderProcessor(Builder builder) {
		MultiJobBuilder b = (MultiJobBuilder) builder;
		super.phases = new ArrayList<StructurePhase>();
		List<AbstractProject> items = new ArrayList<AbstractProject>();
		AbstractProject tmpProject;
		for (PhaseJobsConfig config : b.getPhaseJobs()) {
			tmpProject = (AbstractProject) Jenkins.getInstance().getItem(config.getJobName());
			if (tmpProject != null) {
				items.add(tmpProject);
			} else {
				logger.severe("project named '" + config.getJobName() + "' not found; considering this as corrupted configuration and skipping the project");
			}
		}
		StructurePhase newPhase = new StructurePhase();
		newPhase.setName(b.getPhaseName());
		newPhase.setBlocking(true);
		newPhase.setJobs(createSubJobs(items));
		super.phases.add(newPhase);
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
