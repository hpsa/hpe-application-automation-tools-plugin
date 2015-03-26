package com.hp.octane.plugins.jenkins.model.processors.parameters;

import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import hudson.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 19/02/2015.
 */
public abstract class AbstractParametersProcessor {
	public static ParameterConfig[] getConfigs(AbstractProject project) {
		ParameterConfig[] result;
		List<ParameterDefinition> paramDefinitions;
		ParameterDefinition pd;
		String className;
		AbstractParametersProcessor processor;

		if (project.isParameterized()) {
			paramDefinitions = ((ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class)).getParameterDefinitions();
			result = new ParameterConfig[paramDefinitions.size()];
			for (int i = 0; i < result.length; i++) {
				pd = paramDefinitions.get(i);
				className = pd.getClass().getName();
				processor = ParameterProcessors.getAppropriate(className);
				result[i] = processor.createParameterConfig(pd);
			}
		} else {
			result = new ParameterConfig[0];
		}

		return result;
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
					if (parametersValues.get(j).getName().equals(pd.getName())) {
						pv = parametersValues.get(j);
						parametersValues.remove(j);
						break;
					}
				}
				processor = ParameterProcessors.getAppropriate(className);
				result[i] = processor.createParameterInstance(pd, pv);
			}
		} else {
			result = new ParameterInstance[0];
		}

		return result;
	}

	public abstract boolean isAppropriate(String className);

	public abstract ParameterConfig createParameterConfig(ParameterDefinition pd);

	public abstract ParameterInstance createParameterInstance(ParameterDefinition pd, ParameterValue pv);
}
