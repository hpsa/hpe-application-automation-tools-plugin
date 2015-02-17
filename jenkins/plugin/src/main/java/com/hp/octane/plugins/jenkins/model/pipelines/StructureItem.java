package com.hp.octane.plugins.jenkins.model.pipelines;

import com.hp.octane.plugins.jenkins.model.parameters.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.pipelines.utils.*;
import hudson.model.AbstractProject;
import org.kohsuke.stapler.export.ExportedBean;

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
