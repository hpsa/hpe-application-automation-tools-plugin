package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by gullery on 26/03/2015.
 */

public enum ParameterProcessors {
	//	DYNAMIC("com.seitenbau.jenkins.plugins.dynamicparameter", DynamicParameterProcessor.class),
	EXTENDED("com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterDefinition", ExtendedChoiceParameterProcessor.class),
	INHERENT("hudson.model", InherentParameterProcessor.class),
	NODE_LABEL("org.jvnet.jenkins.plugins.nodelabelparameter", NodeLabelParameterProcessor.class),
	RANDOM_STRING("hudson.plugins.random_string_parameter.RandomStringParameterDefinition", RandomStringParameterProcessor.class);

	private static final Logger logger = Logger.getLogger(ParameterProcessors.class.getName());
	private String targetPluginClassName;
	private Class<? extends AbstractParametersProcessor> processorClass;

	ParameterProcessors(String targetPluginClassName, Class<? extends AbstractParametersProcessor> processorClass) {
		this.targetPluginClassName = targetPluginClassName;
		this.processorClass = processorClass;
	}

	public static ParameterConfig[] getConfigs(AbstractProject project) {
		ArrayList<ParameterConfig> result = new ArrayList<ParameterConfig>();

		List<ParameterDefinition> paramDefinitions;
		ParameterDefinition pd;
		String className;
		AbstractParametersProcessor processor;
		if (project.isParameterized()) {
			paramDefinitions = ((ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class)).getParameterDefinitions();
			for (int i = 0; i < paramDefinitions.size(); i++) {
				pd = paramDefinitions.get(i);
				className = pd.getClass().getName();
				processor = getAppropriate(className);
				result.add(processor.createParameterConfig(pd));
			}
		}

		if (project instanceof MatrixProject) {
			AxisList axisList = ((MatrixProject) project).getAxes();
			for (Axis axis : axisList) {
				result.add(new ParameterConfig(axis.getName(), ParameterType.AXIS, new ArrayList<Object>(axis.getValues())));
			}
		}
		ParameterConfig[] params = new ParameterConfig[result.size()];
		return result.toArray(params);
	}

	//  TODO: the below mapping between param configs and values based on param name uniqueness, beware!
	public static ParameterInstance[] getInstances(AbstractBuild build) {
		List<ParameterInstance> result = new ArrayList<ParameterInstance>();
		AbstractProject project = build.getProject();
		List<ParameterDefinition> paramDefinitions;
		String className;

		AbstractParametersProcessor processor;
		List<ParameterValue> parametersValues;
		ParametersAction parametersAction = build.getAction(ParametersAction.class);
		if (parametersAction != null) {
			parametersValues = new ArrayList<ParameterValue>(parametersAction.getParameters());
		} else {
			parametersValues = new ArrayList<ParameterValue>();
		}
		ParameterValue pv;

		//  TODO: should be moved to the Matrix Project processor
		if (project instanceof MatrixConfiguration) {
			Combination combination = ((MatrixConfiguration) project).getCombination();
			for (Map.Entry<String, String> entry : combination.entrySet()) {
				result.add(new ParameterInstance(new ParameterConfig(entry.getKey(), ParameterType.AXIS, Collections.emptyList()), entry.getValue()));
			}
		}

		if (project.isParameterized()) {
			paramDefinitions = ((ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class)).getParameterDefinitions();
			for (ParameterDefinition pd : paramDefinitions) {
				className = pd.getClass().getName();
				pv = null;

				try {
					for (int j = 0; j < parametersValues.size(); j++) {
						if (parametersValues.get(j) != null && parametersValues.get(j).getName().equals(pd.getName())) {
							pv = parametersValues.get(j);
							parametersValues.remove(j);
							break;
						}
					}
					processor = getAppropriate(className);
					result.add(processor.createParameterInstance(pd, pv));
				} catch (Exception e) {
					logger.severe("failed to process instance of parameter or type '" + className + "'");
				}
			}
		}

		return result.toArray(new ParameterInstance[result.size()]);
	}

	private static AbstractParametersProcessor getAppropriate(String className) {
		for (ParameterProcessors p : values()) {
			if (className.startsWith(p.targetPluginClassName)) {
				try {
					return p.processorClass.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return new UnsupportedParameterProcessor();
	}
}
