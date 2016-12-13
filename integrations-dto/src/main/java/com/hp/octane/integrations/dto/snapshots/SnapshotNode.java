package com.hp.octane.integrations.dto.snapshots;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.scm.SCMData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

public interface SnapshotNode extends DTOBase {

	String getJobCiId();

	SnapshotNode setJobCiId(String jobCiId);

	String getName();

	SnapshotNode setName(String name);

	String getBuildCiId();

	SnapshotNode setBuildCiId(String buildCiId);

	String getNumber();

	SnapshotNode setNumber(String number);

	List<CIEventCause> getCauses();

	SnapshotNode setCauses(List<CIEventCause> causes);

	CIBuildStatus getStatus();

	SnapshotNode setStatus(CIBuildStatus status);

	CIBuildResult getResult();

	SnapshotNode setResult(CIBuildResult result);

	Long getEstimatedDuration();

	SnapshotNode setEstimatedDuration(Long estimatedDuration);

	Long getStartTime();

	SnapshotNode setStartTime(Long startTime);

	Long getDuration();

	SnapshotNode setDuration(Long duration);

	SCMData getScmData();

	SnapshotNode setScmData(SCMData scmData);

	List<CIParameter> getParameters();

	SnapshotNode setParameters(List<CIParameter> parameters);

	List<SnapshotPhase> getPhasesInternal();

	SnapshotNode setPhasesInternal(List<SnapshotPhase> phasesInternal);

	List<SnapshotPhase> getPhasesPostBuild();

	SnapshotNode setPhasesPostBuild(List<SnapshotPhase> phasesPostBuild);
}
