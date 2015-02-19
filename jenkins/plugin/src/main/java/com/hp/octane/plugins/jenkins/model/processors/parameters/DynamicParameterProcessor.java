package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import com.seitenbau.jenkins.plugins.dynamicparameter.ChoiceParameterDefinition;
import com.seitenbau.jenkins.plugins.dynamicparameter.StringParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

/**
 * Created by gullery on 19/02/2015.
 */

public class DynamicParameterProcessor extends AbstractParametersProcessor {
	private static final DynamicParameterProcessor instance;

	static {
		instance = new DynamicParameterProcessor();
	}

	private DynamicParameterProcessor() {
	}

	public static DynamicParameterProcessor getInstance() {
		return instance;
	}

	@Override
	public ParameterConfig createParameterConfig(ParameterDefinition pd) {
		if (pd instanceof StringParameterDefinition) {
			StringParameterDefinition stringPd = (StringParameterDefinition) pd;
			return new ParameterConfig(pd, ParameterType.STRING);
		} else if (pd instanceof ChoiceParameterDefinition) {
			ChoiceParameterDefinition choicePd = (ChoiceParameterDefinition) pd;
			return new ParameterConfig(pd, ParameterType.STRING, choicePd.getChoices());
		} else {
			return new ParameterConfig(pd);
		}
	}

	@Override
	public ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		return new ParameterInstance(createParameterConfig(pd), pv.getValue());
	}
}
