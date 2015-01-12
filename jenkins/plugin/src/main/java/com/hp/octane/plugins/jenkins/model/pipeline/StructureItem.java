package com.hp.octane.plugins.jenkins.model.pipeline;

import com.hp.octane.plugins.jenkins.model.pipeline.utils.*;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public final class StructureItem extends AbstractItem {
	private ParameterConfig[] parameters;
	private StructurePhase[] internals;
	private StructurePhase[] postBuilds;

	public StructureItem(AbstractProject project) {
		super(project.getName());
		parameters = super.getParameterConfigs(project);
		AbstractProjectProcessor flowProcessor = super.getFlowProcessor(project);
		internals = flowProcessor.getInternals();
		postBuilds = flowProcessor.getPostBuilds();
	}

	@Override
	ParameterConfig[] provideParameters() {
		return parameters;
	}

	@Override
	AbstractPhase[] providePhasesInternal() {
		return internals;
	}

	@Override
	AbstractPhase[] providePhasesPostBuilds() {
		return postBuilds;
	}
}
