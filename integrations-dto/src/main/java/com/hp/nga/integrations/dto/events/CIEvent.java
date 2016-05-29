package com.hp.nga.integrations.dto.events;

import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.causes.CIEventCause;
import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.nga.integrations.dto.snapshots.CIBuildResult;

import java.util.List;

/**
 * User: gullery
 * <p/>
 * CI Event data object descriptor
 */

public interface CIEvent extends DTOBase {

	String getProjectName();

	CIEvent setProjectName(String projectName);

	CIEventType getEventType();

	CIEvent setEventType(CIEventType type);

	String getBuildCiId();

	CIEvent setBuildCiId(String buildCiId);

	String getProject();

	CIEvent setProject(String project);

	String getNumber();

	CIEvent setNumber(String number);

	List<CIEventCause> getCauses();

	CIEvent setCauses(List<CIEventCause> causes);

	List<CIParameter> getParameters();

	CIEvent setParameters(List<CIParameter> parameters);

	CIBuildResult getResult();

	CIEvent setResult(CIBuildResult result);

	Long getStartTime();

	CIEvent setStartTime(Long startTime);

	Long getEstimatedDuration();

	CIEvent setEstimatedDuration(Long estimatedDuration);

	Long getDuration();

	CIEvent setDuration(Long duration);

	SCMData getScmData();

	CIEvent setScmData(SCMData scmData);
}
