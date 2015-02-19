package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

/**
 * Created by gullery on 19/02/2015.
 */

public class UnsupportedParameterProcessor extends AbstractParametersProcessor {
	private static final UnsupportedParameterProcessor instance;

	static {
		instance = new UnsupportedParameterProcessor();
	}

	private UnsupportedParameterProcessor() {
	}

	public static UnsupportedParameterProcessor getInstance() {
		return instance;
	}

	@Override
	public ParameterConfig createParameterConfig(ParameterDefinition pd) {
		return new ParameterConfig(pd);
	}

	@Override
	public ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		return new ParameterInstance(createParameterConfig(pd), null);
	}
}
