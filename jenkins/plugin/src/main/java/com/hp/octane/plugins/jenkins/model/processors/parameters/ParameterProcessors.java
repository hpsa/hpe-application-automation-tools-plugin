package com.hp.octane.plugins.jenkins.model.processors.parameters;

/**
 * Created by gullery on 26/03/2015.
 */

public enum ParameterProcessors {
	//	DYNAMIC(new DynamicParameterProcessor()),
	EXTENDED(new ExtendedChoiceParameterProcessor()),
	INHERENT(new InherentParameterProcessor()),
	NODE_LABEL(new NodeLabelParameterProcessor()),
	RANDOM_STRING(new RandomStringParameterProcessor()),
	UNSUPPORTED(new UnsupportedParameterProcessor());

	private AbstractParametersProcessor processor;

	ParameterProcessors(AbstractParametersProcessor processor) {
		this.processor = processor;
	}

	public AbstractParametersProcessor getProcessor() {
		return processor;
	}

	public static AbstractParametersProcessor getAppropriate(String className) {
		for (ParameterProcessors p : values()) {
			if (p.processor.isAppropriate(className)) {
				return p.processor;
			}
		}
		return UNSUPPORTED.processor;
	}
}
