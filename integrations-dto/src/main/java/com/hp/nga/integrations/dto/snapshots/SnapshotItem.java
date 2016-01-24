package com.hp.nga.integrations.dto.snapshots;

import com.hp.nga.integrations.dto.causes.CIEventCauseBase;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;
import com.hp.nga.integrations.dto.scm.SCMData;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

public final class SnapshotItem {
	private static final Logger logger = Logger.getLogger(SnapshotItem.class.getName());

	private String name;
	private Integer number = null;
	private CIEventCauseBase[] causes = null;
	private SnapshotStatus status = SnapshotStatus.UNAVAILABLE;
	private SnapshotResult result = SnapshotResult.UNAVAILABLE;
	private Long estimatedDuration = null;
	private Long startTime = null;
	private Long duration = null;
	private SCMData scmData = null;
	private List<ParameterInstance> parameters;
	private List<SnapshotPhase> internals;
	private List<SnapshotPhase> postBuilds;

	public static Logger getLogger() {
		return logger;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public CIEventCauseBase[] getCauses() {
		return causes;
	}

	public void setCauses(CIEventCauseBase[] causes) {
		this.causes = causes;
	}

	public SnapshotStatus getStatus() {
		return status;
	}

	public void setStatus(SnapshotStatus status) {
		this.status = status;
	}

	public SnapshotResult getResult() {
		return result;
	}

	public void setResult(SnapshotResult result) {
		this.result = result;
	}

	public Long getEstimatedDuration() {
		return estimatedDuration;
	}

	public void setEstimatedDuration(Long estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public SCMData getScmData() {
		return scmData;
	}

	public void setScmData(SCMData scmData) {
		this.scmData = scmData;
	}

	public List<ParameterInstance> getParameters() {
		return parameters;
	}

	public void setParameters(List<ParameterInstance> parameters) {
		this.parameters = parameters;
	}

	public List<SnapshotPhase> getInternals() {
		return internals;
	}

	public void setInternals(List<SnapshotPhase> internals) {
		this.internals = internals;
	}

	public List<SnapshotPhase> getPostBuilds() {
		return postBuilds;
	}

	public void setPostBuilds(List<SnapshotPhase> postBuilds) {
		this.postBuilds = postBuilds;
	}
}
