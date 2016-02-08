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

	public String getName();

	public void setName(String name);

	public Integer getNumber();

	public void setNumber(Integer number);

	public CIEventCauseBase[] getCauses();

	public void setCauses(CIEventCauseBase[] causes);

	public SnapshotStatus getStatus();

	public void setStatus(SnapshotStatus status);

	public SnapshotResult getResult();

	public void setResult(SnapshotResult result);

	public Long getEstimatedDuration();

	public void setEstimatedDuration(Long estimatedDuration);

	public Long getStartTime();

	public void setStartTime(Long startTime);

	public Long getDuration();

	public void setDuration(Long duration);

	public SCMData getScmData();

	public void setScmData(SCMData scmData);

	public List<ParameterInstance> getParameters();

	public void setParameters(List<ParameterInstance> parameters);

	public List<SnapshotPhase> getPhasesInternal();

	public void setPhasesInternal(List<SnapshotPhase> phasesInternal);

	public List<SnapshotPhase> getPhasesPostBuild();

	public void setPhasesPostBuild(List<SnapshotPhase> phasesPostBuild);

	public void setCiId(String ciId);
	public String getCiId();
}
