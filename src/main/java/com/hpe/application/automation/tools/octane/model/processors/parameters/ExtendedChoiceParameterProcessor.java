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

import com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterDefinition;
import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hpe.application.automation.tools.octane.model.ModelFactory;
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
        Object value = pv == null ? null : pv.getValue();
		return ModelFactory.createParameterInstance(createParameterConfig(pd), value);
	}
}
