package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterDefinition;
import com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterValue;
import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gullery on 19/02/2015.
 */

public class ExtendedChoiceParameterProcessor extends AbstractParametersProcessor {
	private static final ExtendedChoiceParameterProcessor instance;

	static {
		instance = new ExtendedChoiceParameterProcessor();
	}

	private ExtendedChoiceParameterProcessor() {
	}

	public static ExtendedChoiceParameterProcessor getInstance() {
		return instance;
	}

	@Override
	public ParameterConfig createParameterConfig(ParameterDefinition pd) {
		ExtendedChoiceParameterDefinition extChoice = (ExtendedChoiceParameterDefinition) pd;
		Map<String, String> choicesMap;
		List<Object> choices = new ArrayList<Object>();
		try {
			choicesMap = extChoice.getChoicesByDropdownId();
		} catch (Exception e) {
			choicesMap = null;
		}
		if (choicesMap != null) {
			choices = new ArrayList<Object>(choicesMap.values());
		}
		return new ParameterConfig(pd, ParameterType.STRING, null, choices);
	}

	@Override
	public ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		ExtendedChoiceParameterValue extValue = (ExtendedChoiceParameterValue) pv;
		return new ParameterInstance(createParameterConfig(pd), extValue.getValue());
	}
}
