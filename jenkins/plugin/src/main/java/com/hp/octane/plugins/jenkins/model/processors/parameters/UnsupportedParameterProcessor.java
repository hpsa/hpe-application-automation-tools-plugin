package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.pipelines.PipelinesFactory;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

/**
 * Created by gullery on 19/02/2015.
 */

public class UnsupportedParameterProcessor extends AbstractParametersProcessor {
	UnsupportedParameterProcessor() {
	}

	@Override
	public ParameterConfig createParameterConfig(ParameterDefinition pd) {
		return PipelinesFactory.createParameterConfig(pd);
	}

	@Override
	public ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		return new ParameterInstance(createParameterConfig(pd));
	}
}
