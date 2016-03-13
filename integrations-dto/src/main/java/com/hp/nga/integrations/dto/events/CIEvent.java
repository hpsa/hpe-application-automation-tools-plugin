package com.hp.nga.integrations.dto.events;

import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.causes.CIEventCause;
import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.nga.integrations.dto.snapshots.CIBuildResult;

/**
 * User: gullery
 * <p/>
 * CI Event data object descriptor
 */

public interface CIEvent extends DTOBase {

	CIEventType getEventType();

	CIEvent setEventType(CIEventType type);

	String getProject();

	CIEvent setProject(String project);

	String getNumber();

	CIEvent setNumber(String number);



	CIEventCause[] getCauses();

	CIEvent setCauses(CIEventCause[] causes);

	CIParameter[] getParameters();

	CIEvent setParameters(CIParameter[] parameters);

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
