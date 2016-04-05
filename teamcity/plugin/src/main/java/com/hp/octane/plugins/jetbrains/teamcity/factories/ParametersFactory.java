package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.nga.integrations.dto.parameters.CIParameterType;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by gullery on 22/03/2016.
 */

public class ParametersFactory {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	public List<CIParameter> obtainFromBuildType(SBuildType buildType) {
		List<CIParameter> result = new LinkedList<CIParameter>();
		CIParameter tmp;

		if (buildType != null && !buildType.getParameters().isEmpty()) {
			for (Map.Entry<String, String> parameter : buildType.getParameters().entrySet()) {
				tmp = dtoFactory.newDTO(CIParameter.class)
						.setType(CIParameterType.STRING)
						.setName(parameter.getKey())
						.setDescription("Value location: " + parameter.getValue());
				result.add(tmp);
			}
		}

		return result;
	}

	public List<CIParameter> obtainFromBuild(SBuild build) {
		List<CIParameter> result = new LinkedList<CIParameter>();
		CIParameter tmp;

		if (build != null && !build.getBuildOwnParameters().isEmpty()) {
			for (Map.Entry<String, String> parameter : build.getBuildOwnParameters().entrySet()) {
				tmp = dtoFactory.newDTO(CIParameter.class)
						.setType(CIParameterType.STRING)
						.setName(parameter.getKey())
						.setValue(parameter.getValue());
				result.add(tmp);
			}
		}

		return result;
	}
}
