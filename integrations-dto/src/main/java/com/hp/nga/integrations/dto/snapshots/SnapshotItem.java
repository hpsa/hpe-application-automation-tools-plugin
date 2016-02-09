package com.hp.nga.integrations.dto.snapshots;

import com.hp.nga.integrations.dto.causes.CIEventCauseBase;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;
import com.hp.nga.integrations.dto.scm.SCMData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

public interface SnapshotItem {

	String getCiId();

	SnapshotItem setCiId(String ciId);

	String getName();

	SnapshotItem setName(String name);

	Integer getNumber();

	SnapshotItem setNumber(Integer number);

	CIEventCauseBase[] getCauses();

	SnapshotItem setCauses(CIEventCauseBase[] causes);

	SnapshotStatus getStatus();

	SnapshotItem setStatus(SnapshotStatus status);

	SnapshotResult getResult();

	SnapshotItem setResult(SnapshotResult result);

	Long getEstimatedDuration();

	SnapshotItem setEstimatedDuration(Long estimatedDuration);

	Long getStartTime();

	SnapshotItem setStartTime(Long startTime);

	Long getDuration();

	SnapshotItem setDuration(Long duration);

	SCMData getScmData();

	SnapshotItem setScmData(SCMData scmData);

	List<ParameterInstance> getParameters();

	SnapshotItem setParameters(List<ParameterInstance> parameters);

	List<SnapshotPhase> getPhasesInternal();

	SnapshotItem setPhasesInternal(List<SnapshotPhase> phasesInternal);

	List<SnapshotPhase> getPhasesPostBuild();

	SnapshotItem setPhasesPostBuild(List<SnapshotPhase> phasesPostBuild);
}
