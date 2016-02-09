package com.hp.nga.integrations.dto.pipelines;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

public interface StructureItem {

	String getCiId();

	StructureItem setCiId(String ciId);

	String getName();

	StructureItem setName(String name);

	List<ParameterConfig> getParameters();

	StructureItem setParameters(List<ParameterConfig> parameters);

	List<StructurePhase> getPhasesInternal();

	StructureItem setPhasesInternal(List<StructurePhase> phasesInternal);

	List<StructurePhase> getPhasesPostBuild();

	StructureItem setPhasesPostBuild(List<StructurePhase> phasesPostBuild);
}
