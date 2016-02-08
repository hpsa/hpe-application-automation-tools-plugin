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


	public String getName();

	public void setName(String name) ;

	public List<ParameterConfig> getParameters();

	public void setParameters(List<ParameterConfig> parameters);

	public List<StructurePhase> getPhasesInternal();

	public void setPhasesInternal(List<StructurePhase> phasesInternal);

	public List<StructurePhase> getPhasesPostBuild() ;

	public void setPhasesPostBuild(List<StructurePhase> phasesPostBuild);

	public void setCiId(String ciId);

	public String getCiId() ;
}
