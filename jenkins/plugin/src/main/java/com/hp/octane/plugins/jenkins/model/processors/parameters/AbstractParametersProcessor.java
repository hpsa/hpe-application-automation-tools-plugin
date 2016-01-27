package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

/**
 * Created by gullery on 19/02/2015.
 */
public abstract class AbstractParametersProcessor {
	public abstract ParameterConfig createParameterConfig(ParameterDefinition pd);

	public abstract ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv);
}
