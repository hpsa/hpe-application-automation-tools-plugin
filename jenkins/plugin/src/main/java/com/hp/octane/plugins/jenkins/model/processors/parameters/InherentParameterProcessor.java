package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.parameters.*;
import hudson.model.*;

import java.util.ArrayList;

/**
 * Created by gullery on 19/02/2015.
 */

public class InherentParameterProcessor extends AbstractParametersProcessor {
	private static final InherentParameterProcessor instance;

	static {
		instance = new InherentParameterProcessor();
	}

	private InherentParameterProcessor() {
	}

	public static InherentParameterProcessor getInstance() {
		return instance;
	}

	@Override
	public ParameterConfig createParameterConfig(ParameterDefinition pd) {
		ParameterConfig result;
		if (pd instanceof BooleanParameterDefinition) {
			result = new ParameterConfig(pd, ParameterType.BOOLEAN);
		} else if (pd instanceof TextParameterDefinition) {
			result = new ParameterConfig(pd, ParameterType.STRING);
		} else if (pd instanceof StringParameterDefinition) {
			result = new ParameterConfig(pd, ParameterType.STRING);
		} else if (pd instanceof ChoiceParameterDefinition) {
			ChoiceParameterDefinition choicePd = (ChoiceParameterDefinition) pd;
			result = new ParameterConfig(pd, ParameterType.STRING, null, new ArrayList<Object>(choicePd.getChoices()));
		} else if (pd instanceof PasswordParameterDefinition) {
			PasswordParameterDefinition passPd = (PasswordParameterDefinition) pd;
			result = new ParameterConfig(pd, ParameterType.PASSWORD, passPd.getDefaultValue());
		} else if (pd instanceof FileParameterDefinition) {
			result = new ParameterConfig(pd, ParameterType.FILE);
		} else {
			result = UnsupportedParameterProcessor.getInstance().createParameterConfig(pd);
		}
		return result;
	}

	@Override
	public ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv) {
		ParameterInstance result;
		ParameterConfig pc = createParameterConfig(pd);
		if (pd instanceof BooleanParameterDefinition) {
			result = new ParameterInstance(pc, pv.getValue());
		} else if (pd instanceof TextParameterDefinition) {
			result = new ParameterInstance(pc, pv.getValue());
		} else if (pd instanceof StringParameterDefinition) {
			result = new ParameterInstance(pc, pv.getValue());
		} else if (pd instanceof ChoiceParameterDefinition) {
			result = new ParameterInstance(pc, pv.getValue());
		} else if (pd instanceof PasswordParameterDefinition) {
			result = new ParameterInstance(pc, "");
		} else if (pd instanceof FileParameterDefinition) {
			FileParameterValue filePv = (FileParameterValue) pv;
			result = new ParameterInstance(pc, filePv == null ? null : filePv.getLocation());
		} else {
			result = UnsupportedParameterProcessor.getInstance().createParameterInstance(pd, pv);
		}
		return result;
	}
}
