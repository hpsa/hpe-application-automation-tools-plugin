package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.api.parameters.CIParameter;
import com.hp.octane.integrations.dto.api.parameters.CIParameterType;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;

/**
 * Created by gullery on 19/02/2015.
 */

public class UnsupportedParameterProcessor extends AbstractParametersProcessor {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	UnsupportedParameterProcessor() {
	}

	@Override
	public CIParameter createParameterConfig(ParameterDefinition pd) {
		return ModelFactory.createParameterConfig(pd);
	}

	@Override
	public CIParameter createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		return dtoFactory.newDTO(CIParameter.class)
				.setType(CIParameterType.UNKNOWN)
				.setName(pd.getName())
				.setDescription(pd.getDescription())
				.setDefaultValue(pd.getDefaultParameterValue() != null ? pd.getDefaultParameterValue().getValue() : null);
	}
}
