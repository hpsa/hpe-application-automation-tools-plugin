package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.plugins.random_string_parameter.RandomStringParameterDefinition;
import hudson.plugins.random_string_parameter.RandomStringParameterValue;

/**
 * Created by gullery on 19/02/2015.
 */

public class RandomStringParameterProcessor extends AbstractParametersProcessor {
	RandomStringParameterProcessor() {
	}

	@Override
	public CIParameter createParameterConfig(ParameterDefinition pd) {
		RandomStringParameterDefinition randomPd = (RandomStringParameterDefinition) pd;
		return ModelFactory.createParameterConfig(pd, CIParameterType.STRING);
	}

	@Override
	public CIParameter createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		RandomStringParameterDefinition randomPd = (RandomStringParameterDefinition) pd;
		RandomStringParameterValue randomPv = (RandomStringParameterValue) pv;
		return ModelFactory.createParameterInstance(createParameterConfig(pd), randomPv);
	}
}
