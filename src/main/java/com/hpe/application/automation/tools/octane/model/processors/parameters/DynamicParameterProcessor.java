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

import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hpe.application.automation.tools.octane.model.ModelFactory;
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
	public CIParameter createParameterConfig(ParameterDefinition pd) {
		if (pd instanceof StringParameterDefinition) {
			StringParameterDefinition stringPd = (StringParameterDefinition) pd;
			return ModelFactory.createParameterConfig(pd, CIParameterType.STRING);
		} else if (pd instanceof ChoiceParameterDefinition) {
			ChoiceParameterDefinition choicePd = (ChoiceParameterDefinition) pd;
			return ModelFactory.createParameterConfig(pd, CIParameterType.STRING, null, choicePd.getChoices());
		} else {
			return ModelFactory.createParameterConfig(pd);
		}
	}

	@Override
	public CIParameter createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
	    Object value = pv == null ? null : pv.getValue();
		return ModelFactory.createParameterInstance(createParameterConfig(pd), value);
	}
}
