package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.pipelines.ModelFactory;
import com.seitenbau.jenkins.plugins.dynamicparameter.ChoiceParameterDefinition;
import com.seitenbau.jenkins.plugins.dynamicparameter.StringParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

/**
 * Created by gullery on 19/02/2015.
 */

public class DynamicParameterProcessor extends AbstractParametersProcessor {
	DynamicParameterProcessor() {
	}

	@Override
	public ParameterConfig createParameterConfig(ParameterDefinition pd) {
		if (pd instanceof StringParameterDefinition) {
			StringParameterDefinition stringPd = (StringParameterDefinition) pd;
			return ModelFactory.createParameterConfig(pd, ParameterType.STRING);
		} else if (pd instanceof ChoiceParameterDefinition) {
			ChoiceParameterDefinition choicePd = (ChoiceParameterDefinition) pd;
			return ModelFactory.createParameterConfig(pd, ParameterType.STRING, null, choicePd.getChoices());
		} else {
			return ModelFactory.createParameterConfig(pd);
		}
	}

	@Override
	public ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		return ModelFactory.createParameterInstance(createParameterConfig(pd), pv);
	}
}
