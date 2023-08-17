package com.microfocus.application.automation.tools.octane.model.processors.parameters;

import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.microfocus.application.automation.tools.octane.model.ModelFactory;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

public class ActiveChoiceParameterProcessor extends AbstractParametersProcessor {
	ActiveChoiceParameterProcessor() {
	}

	@Override
	public CIParameter createParameterConfig(ParameterDefinition pd) {
		return ModelFactory.createParameterConfig(pd, CIParameterType.STRING, "");
	}

	@Override
	public CIParameter createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		Object value = pv == null ? null : pv.getValue();
		return ModelFactory.createParameterInstance(createParameterConfig(pd), value);
	}
}
