package com.hp.octane.plugins.jenkins.model.pipelines;

import com.hp.octane.plugins.jenkins.model.api.AbstractItem;
import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.processors.parameters.AbstractParametersProcessor;
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
public final class StructureItem extends AbstractItem<ParameterConfig, StructurePhase> {
	public StructureItem(AbstractProject project) {
		super(project);
		setParameters(AbstractParametersProcessor.getConfigs(project));
		setInternals(super.getFlowProcessor().getInternals());
		setPostBuilds(super.getFlowProcessor().getPostBuilds());
	}
}
