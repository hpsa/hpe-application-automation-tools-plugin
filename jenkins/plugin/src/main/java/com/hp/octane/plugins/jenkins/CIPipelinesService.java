package com.hp.octane.plugins.jenkins;

import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.octane.plugins.jenkins.model.processors.projects.AbstractProjectProcessor;
import hudson.model.AbstractProject;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;

import java.util.Arrays;

public class CIPipelinesService {

	public static StructureItem getPipeline(String rootCIJobId) {
		StructureItem result = null;
		TopLevelItem item = Jenkins.getInstance().getItem(rootCIJobId);
		AbstractProject project;
		AbstractProjectProcessor projectProcessor;
		if (item != null && item instanceof AbstractProject) {
			project = (AbstractProject) item;
			result = new StructureItem();
			result.setName(project.getName());
			//  TODO: parameters
			projectProcessor = AbstractProjectProcessor.getFlowProcessor(project);
			result.setInternals(Arrays.asList(projectProcessor.getInternals()));
			result.setPostBuilds(Arrays.asList(projectProcessor.getPostBuilds()));
		}
		return result;
	}
}
