package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.pipelines.PipelinesFactory;
import hudson.model.*;

import java.util.ArrayList;

/**
 * Created by gullery on 19/02/2015.
 */

public class InherentParameterProcessor extends AbstractParametersProcessor {
	InherentParameterProcessor() {
	}

	@Override
	public ParameterConfig createParameterConfig(ParameterDefinition pd) {
		ParameterConfig result;
		if (pd instanceof BooleanParameterDefinition) {
			result = PipelinesFactory.createParameterConfig(pd, ParameterType.BOOLEAN);
		} else if (pd instanceof TextParameterDefinition) {
			result = PipelinesFactory.createParameterConfig(pd, ParameterType.STRING);
		} else if (pd instanceof StringParameterDefinition) {
			result = PipelinesFactory.createParameterConfig(pd, ParameterType.STRING);
		} else if (pd instanceof ChoiceParameterDefinition) {
			ChoiceParameterDefinition choicePd = (ChoiceParameterDefinition) pd;
			result = PipelinesFactory.createParameterConfig(pd, ParameterType.STRING, null, new ArrayList<Object>(choicePd.getChoices()));
		} else if (pd instanceof PasswordParameterDefinition) {
			PasswordParameterDefinition passPd = (PasswordParameterDefinition) pd;
			result = PipelinesFactory.createParameterConfig(pd, ParameterType.PASSWORD, passPd.getDefaultValue());
		} else if (pd instanceof FileParameterDefinition) {
			result = PipelinesFactory.createParameterConfig(pd, ParameterType.FILE);
		} else {
			result = new UnsupportedParameterProcessor().createParameterConfig(pd);
		}
		return result;
	}

	@Override
	public ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		ParameterInstance result;
		ParameterConfig pc = createParameterConfig(pd);
		if (pd instanceof BooleanParameterDefinition) {
			result = new ParameterInstance(pc, pv);
		} else if (pd instanceof TextParameterDefinition) {
			result = new ParameterInstance(pc, pv);
		} else if (pd instanceof StringParameterDefinition) {
			result = new ParameterInstance(pc, pv);
		} else if (pd instanceof ChoiceParameterDefinition) {
			result = new ParameterInstance(pc, pv);
		} else if (pd instanceof PasswordParameterDefinition) {
			result = new ParameterInstance(pc, "");
		} else if (pd instanceof FileParameterDefinition) {
			FileParameterValue filePv = (FileParameterValue) pv;
			result = filePv != null ?
					new ParameterInstance(pc, filePv.getOriginalFileName()) :
					new ParameterInstance(pc);
		} else {
			result = new UnsupportedParameterProcessor().createParameterInstance(pd, pv);
		}
		return result;
	}
}
