package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.nga.integrations.dto.parameters.CIParameterType;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import hudson.matrix.*;
import hudson.model.*;

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
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private String targetPluginClassName;
	private Class<? extends AbstractParametersProcessor> processorClass;

	ParameterProcessors(String targetPluginClassName, Class<? extends AbstractParametersProcessor> processorClass) {
		this.targetPluginClassName = targetPluginClassName;
		this.processorClass = processorClass;
	}

	public static List<CIParameter> getConfigs(AbstractProject project) {
		ArrayList<CIParameter> result = new ArrayList<CIParameter>();

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
				result.add(ModelFactory.createParameterConfig(axis.getName(), CIParameterType.AXIS, new ArrayList<Object>(axis.getValues())));
			}
		}
//		ParameterConfig[] params = new ParameterConfig[result.size()];
//		return result.toArray(params);
		return result;
	}

	//  TODO: the below mapping between param configs and values based on param name uniqueness, beware!
	public static List<CIParameter> getInstances(AbstractBuild build) {
		List<CIParameter> result = new ArrayList<CIParameter>();
		CIParameter tmp;
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
				tmp = dtoFactory.newDTO(CIParameter.class)
						.setType(CIParameterType.AXIS)
						.setName(entry.getKey())
						.setValue(entry.getValue());
				result.add(tmp);
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
					logger.severe("failed to process instance of parameter or type '" + className + "', adding as unsupported");
					result.add(new UnsupportedParameterProcessor().createParameterInstance(pd, pv));
				}
			}
		}

		return result;

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
