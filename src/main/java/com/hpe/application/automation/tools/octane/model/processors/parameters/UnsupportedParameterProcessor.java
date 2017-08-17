/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.model.processors.parameters;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hpe.application.automation.tools.octane.model.ModelFactory;
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
