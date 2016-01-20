package com.hp.nga.dto.builds;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 15/01/2016.
 */

public class SnapshotNodeDTO {
	private String ciId;
	private Long startTime;
	private Long duration;
	private Long estimatedDuration;
	private SnapshotNodeStatus status;
	private SnapshotNodeResult result;
	private List<SnapshotNodePhaseDTO> phases = new ArrayList<SnapshotNodePhaseDTO>();

	public String getCiId() {
		return ciId;
	}

	public void setCiBuildId(String ciId) {
		this.ciId = ciId;
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

	public Long getEstimatedDuration() {
		return estimatedDuration;
	}

	public void setEstimatedDuration(Long estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	public SnapshotNodeStatus getStatus() {
		return status;
	}

	public void setStatus(SnapshotNodeStatus status) {
		this.status = status;
	}

	public SnapshotNodeResult getResult() {
		return result;
	}

	public void setResult(SnapshotNodeResult result) {
		this.result = result;
	}

	public List<SnapshotNodePhaseDTO> getPhases() {
		return phases;
	}

	public void setPhases(List<SnapshotNodePhaseDTO> phases) {
		this.phases = phases;
	}
}
