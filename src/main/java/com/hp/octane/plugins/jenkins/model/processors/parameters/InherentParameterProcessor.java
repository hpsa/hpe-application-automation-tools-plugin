package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
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
