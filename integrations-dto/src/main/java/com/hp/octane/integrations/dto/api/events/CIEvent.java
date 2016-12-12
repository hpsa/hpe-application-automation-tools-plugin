package com.hp.octane.integrations.dto.api.events;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.api.causes.CIEventCause;
import com.hp.octane.integrations.dto.api.parameters.CIParameter;
import com.hp.octane.integrations.dto.api.scm.SCMData;
import com.hp.octane.integrations.dto.api.snapshots.CIBuildResult;

import java.util.List;

/**
 * User: gullery
 * <p/>
 * CI Event data object descriptor
 */

public interface CIEvent extends DTOBase {

	String getProjectDisplayName();

	CIEvent setProjectDisplayName(String projectDisplayName);

	CIEventType getEventType();

	CIEvent setEventType(CIEventType type);

	String getBuildCiId();

	CIEvent setPhaseType(PhaseType phaseType);

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
