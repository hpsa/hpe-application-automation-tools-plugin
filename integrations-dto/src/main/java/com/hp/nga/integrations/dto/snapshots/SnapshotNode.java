package com.hp.nga.integrations.dto.snapshots;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hp.nga.integrations.dto.DTO;
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

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = SnapshotNodeImpl.class, name = "SnapshotNodeImpl"))
public interface SnapshotNode extends DTO {

	String getCiId();

	SnapshotNode setCiId(String ciId);

	String getName();

	SnapshotNode setName(String name);

	Integer getNumber();

	SnapshotNode setNumber(Integer number);

	CIEventCauseBase[] getCauses();

	SnapshotNode setCauses(CIEventCauseBase[] causes);

	SnapshotStatus getStatus();

	SnapshotNode setStatus(SnapshotStatus status);

	SnapshotResult getResult();

	SnapshotNode setResult(SnapshotResult result);

	Long getEstimatedDuration();

	SnapshotNode setEstimatedDuration(Long estimatedDuration);

	Long getStartTime();

	SnapshotNode setStartTime(Long startTime);

	Long getDuration();

	SnapshotNode setDuration(Long duration);

	SCMData getScmData();

	SnapshotNode setScmData(SCMData scmData);

	List<ParameterInstance> getParameters();

	SnapshotNode setParameters(List<ParameterInstance> parameters);

	List<SnapshotPhase> getPhasesInternal();

	SnapshotNode setPhasesInternal(List<SnapshotPhase> phasesInternal);

	List<SnapshotPhase> getPhasesPostBuild();

	SnapshotNode setPhasesPostBuild(List<SnapshotPhase> phasesPostBuild);
}
