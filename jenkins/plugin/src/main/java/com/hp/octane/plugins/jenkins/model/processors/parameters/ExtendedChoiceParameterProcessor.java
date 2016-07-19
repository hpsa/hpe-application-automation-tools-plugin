package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterDefinition;
import com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterValue;
import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gullery on 19/02/2015.
 */

public class ExtendedChoiceParameterProcessor extends AbstractParametersProcessor {
	ExtendedChoiceParameterProcessor() {
	}

	@Override
	public CIParameter createParameterConfig(ParameterDefinition pd) {
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
		return ModelFactory.createParameterConfig(pd, CIParameterType.STRING, null, choices);
	}

	@Override
	public CIParameter createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		ExtendedChoiceParameterValue extValue = (ExtendedChoiceParameterValue) pv;
		return ModelFactory.createParameterInstance(createParameterConfig(pd), extValue);
	}
}
