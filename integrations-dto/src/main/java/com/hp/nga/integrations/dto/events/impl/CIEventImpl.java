package com.hp.nga.integrations.dto.events.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.causes.CIEventCause;
import com.hp.nga.integrations.dto.events.CIEvent;
import com.hp.nga.integrations.dto.events.CIEventType;
import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.nga.integrations.dto.snapshots.CIBuildResult;

import java.util.List;

/**
 * User: gullery
 * <p/>
 * Base implementation of CI Event object
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIEventImpl implements CIEvent {
	private CIEventType eventType;
	private String buildCiId;
	private String project;
	private String number;
	private List<CIEventCause> causes;
	private List<CIParameter> parameters;
	private CIBuildResult result;
	private Long startTime;
	private Long estimatedDuration;
	private Long duration;
	private SCMData scmData;

	public CIEventType getEventType() {
		return eventType;
	}

	public CIEvent setEventType(CIEventType eventType) {
		this.eventType = eventType;
		return this;
	}

	public String getBuildCiId() {
		return buildCiId;
	}

	public CIEvent setBuildCiId(String buildCiId) {
		this.buildCiId = buildCiId;
		return this;
	}

	public String getProject() {
		return project;
	}

	public CIEvent setProject(String project) {
		this.project = project;
		return this;
	}

	public String getNumber() {
		return number;
	}

	public CIEvent setNumber(String number) {
		this.number = number;
		return this;
	}

	public List<CIEventCause> getCauses() {
		return causes;
	}

	public CIEvent setCauses(List<CIEventCause> causes) {
		this.causes = causes;
		return this;
	}

	public List<CIParameter> getParameters() {
		return parameters;
	}

	public CIEvent setParameters(List<CIParameter> parameters) {
		this.parameters = parameters;
		return this;
	}

	public CIBuildResult getResult() {
		return result;
	}

	public CIEvent setResult(CIBuildResult result) {
		this.result = result;
		return this;
	}

	public Long getStartTime() {
		return startTime;
	}

	public CIEvent setStartTime(Long startTime) {
		this.startTime = startTime;
		return this;
	}

	public Long getEstimatedDuration() {
		return estimatedDuration;
	}

	public CIEvent setEstimatedDuration(Long estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
		return this;
	}

	public Long getDuration() {
		return duration;
	}

	public CIEvent setDuration(Long duration) {
		this.duration = duration;
		return this;
	}

	public SCMData getScmData() {
		return scmData;
	}

	public CIEvent setScmData(SCMData scmData) {
		this.scmData = scmData;
		return this;
	}
}
