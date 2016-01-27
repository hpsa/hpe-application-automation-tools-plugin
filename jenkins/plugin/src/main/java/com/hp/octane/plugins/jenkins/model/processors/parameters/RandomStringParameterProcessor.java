package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.pipelines.ModelFactory;
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
	public ParameterConfig createParameterConfig(ParameterDefinition pd) {
		RandomStringParameterDefinition randomPd = (RandomStringParameterDefinition) pd;
		return ModelFactory.createParameterConfig(pd, ParameterType.STRING);
	}

	@Override
	public ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		RandomStringParameterDefinition randomPd = (RandomStringParameterDefinition) pd;
		RandomStringParameterValue randomPv = (RandomStringParameterValue) pv;
		return ModelFactory.createParameterInstance(createParameterConfig(pd), randomPv);
	}
}
