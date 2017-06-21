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
import hudson.model.*;

import java.util.ArrayList;

/**
 * Created by gullery on 19/02/2015.
 */

public class InherentParameterProcessor extends AbstractParametersProcessor {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	InherentParameterProcessor() {
	}

	@Override
	public CIParameter createParameterConfig(ParameterDefinition pd) {
		CIParameter result;
		if (pd instanceof BooleanParameterDefinition) {
			result = ModelFactory.createParameterConfig(pd, CIParameterType.BOOLEAN);
		} else if (pd instanceof TextParameterDefinition) {
			result = ModelFactory.createParameterConfig(pd, CIParameterType.STRING);
		} else if (pd instanceof StringParameterDefinition) {
			result = ModelFactory.createParameterConfig(pd, CIParameterType.STRING);
		} else if (pd instanceof ChoiceParameterDefinition) {
			ChoiceParameterDefinition choicePd = (ChoiceParameterDefinition) pd;
			result = ModelFactory.createParameterConfig(pd, CIParameterType.STRING, null, new ArrayList<Object>(choicePd.getChoices()));
		} else if (pd instanceof PasswordParameterDefinition) {
			PasswordParameterDefinition passPd = (PasswordParameterDefinition) pd;
			result = ModelFactory.createParameterConfig(pd, CIParameterType.PASSWORD, passPd.getDefaultValue());
		} else if (pd instanceof FileParameterDefinition) {
			result = ModelFactory.createParameterConfig(pd, CIParameterType.FILE);
		} else {
			result = new UnsupportedParameterProcessor().createParameterConfig(pd);
		}
		return result;
	}

	@Override
	public CIParameter createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		CIParameter result;
		CIParameter pc = createParameterConfig(pd);
		Object value = pv == null ? null : pv.getValue();
		if (pd instanceof BooleanParameterDefinition) {
			result = ModelFactory.createParameterInstance(pc, value);
		} else if (pd instanceof TextParameterDefinition) {
			result = ModelFactory.createParameterInstance(pc, value);
		} else if (pd instanceof StringParameterDefinition) {
			result = ModelFactory.createParameterInstance(pc, value);
		} else if (pd instanceof ChoiceParameterDefinition) {
			result = ModelFactory.createParameterInstance(pc, value);
		} else if (pd instanceof PasswordParameterDefinition) {
			result = dtoFactory.newDTO(CIParameter.class)
					.setType(pc.getType())
					.setName(pc.getName())
					.setDescription(pc.getDescription())
					.setChoices(pc.getChoices())
					.setDefaultValue(pc.getDefaultValue())
					.setValue(null);
		} else if (pd instanceof FileParameterDefinition) {
			FileParameterValue filePv = (FileParameterValue) pv;
			result = dtoFactory.newDTO(CIParameter.class)
					.setType(pc.getType())
					.setName(pc.getName())
					.setDescription(pc.getDescription())
					.setChoices(pc.getChoices())
					.setDefaultValue(pc.getDefaultValue())
					.setValue(filePv != null ? filePv.getOriginalFileName() : null);
		} else {
			result = new UnsupportedParameterProcessor().createParameterInstance(pd, pv);
		}
		return result;
	}
}
