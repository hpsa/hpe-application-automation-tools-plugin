package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixProject;
import hudson.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 26/03/2015.
 */

public enum ParameterProcessors {
	//	DYNAMIC("com.seitenbau.jenkins.plugins.dynamicparameter", DynamicParameterProcessor.class),
	EXTENDED("com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterDefinition", ExtendedChoiceParameterProcessor.class),
	INHERENT("hudson.model", InherentParameterProcessor.class),
	NODE_LABEL("org.jvnet.jenkins.plugins.nodelabelparameter", NodeLabelParameterProcessor.class),
	RANDOM_STRING("hudson.plugins.random_string_parameter.RandomStringParameterDefinition", RandomStringParameterProcessor.class);

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

    if(project instanceof MatrixProject) {
      AxisList axisList = ((MatrixProject) project).getAxes();
      for(Axis axis : axisList){
        result.add(new ParameterConfig(axis.getName(), ParameterType.AXIS));
      }
    }
    ParameterConfig[] params = new ParameterConfig[result.size()];
		return result.toArray(params);
	}

	//  TODO: the below mapping between param configs and values based on param name uniqueness, beware!
	public static ParameterInstance[] getInstances(AbstractBuild build) {
		ParameterInstance[] result;
		AbstractProject project = build.getProject();
		List<ParameterDefinition> paramDefinitions;
		ParameterDefinition pd;
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

		if (project.isParameterized()) {
			paramDefinitions = ((ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class)).getParameterDefinitions();
			result = new ParameterInstance[paramDefinitions.size()];
			for (int i = 0; i < result.length; i++) {
				pd = paramDefinitions.get(i);
				className = pd.getClass().getName();
				pv = null;

				for (int j = 0; j < parametersValues.size(); j++) {
					if (parametersValues.get(j) != null && parametersValues.get(j).getName().equals(pd.getName())) {
						pv = parametersValues.get(j);
						parametersValues.remove(j);
						break;
					}
				}
				processor = getAppropriate(className);
				result[i] = processor.createParameterInstance(pd, pv);
			}
		} else {
			result = new ParameterInstance[0];
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
