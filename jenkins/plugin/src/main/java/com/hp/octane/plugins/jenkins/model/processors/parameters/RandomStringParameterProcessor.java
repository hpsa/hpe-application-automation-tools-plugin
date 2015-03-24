package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.plugins.random_string_parameter.RandomStringParameterDefinition;
import hudson.plugins.random_string_parameter.RandomStringParameterValue;

/**
 * Created by gullery on 19/02/2015.
 */

public class RandomStringParameterProcessor extends AbstractParametersProcessor {
	private static final RandomStringParameterProcessor instance = new RandomStringParameterProcessor();

	private RandomStringParameterProcessor() {
	}

	public static RandomStringParameterProcessor getInstance() {
		return instance;
	}

	@Override
	public ParameterConfig createParameterConfig(ParameterDefinition pd) {
		RandomStringParameterDefinition randomPd = (RandomStringParameterDefinition) pd;
		return new ParameterConfig(pd, ParameterType.STRING);
	}

	@Override
	public ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		RandomStringParameterDefinition randomPd = (RandomStringParameterDefinition) pd;
		RandomStringParameterValue randomPv = (RandomStringParameterValue) pv;
		return new ParameterInstance(createParameterConfig(pd), randomPv.value);
	}
}
