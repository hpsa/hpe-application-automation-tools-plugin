package com.hp.nga.integrations.dto.pipelines;

import com.hp.nga.integrations.dto.common.AbstractItem;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

public final class StructureItem extends AbstractItem<ParameterConfig, StructurePhase> {
	public StructureItem(AbstractProject project) {
		super(project);
		setParameters(ParameterProcessors.getConfigs(project));
		setInternals(super.getFlowProcessor().getInternals());
		setPostBuilds(super.getFlowProcessor().getPostBuilds());
	}
}
