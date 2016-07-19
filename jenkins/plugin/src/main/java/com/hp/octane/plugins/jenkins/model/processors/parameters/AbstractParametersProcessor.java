package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.integrations.dto.parameters.CIParameter;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

/**
 * Created by gullery on 19/02/2015.
 */
public abstract class AbstractParametersProcessor {
	public abstract CIParameter createParameterConfig(ParameterDefinition pd);

	public abstract CIParameter createParameterInstance(ParameterDefinition pd, ParameterValue pv);
}
