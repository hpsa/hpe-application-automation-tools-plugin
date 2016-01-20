package com.hp.nga.dto.builds;

import java.util.List;

/**
 * Created by gullery on 15/01/2016.
 */

public class SnapshotNodePhaseDTO {
	private String phaseName;
	private Boolean async = false;
	private List<SnapshotNodeDTO> nodes;

	public String getPhaseName() {
		return phaseName;
	}

	public void setPhaseName(String phaseName) {
		this.phaseName = phaseName;
	}

	public Boolean getAsync() {
		return async;
	}

	public void setAsync(Boolean async) {
		this.async = async;
	}

	public List<SnapshotNodeDTO> getNodes() {
		return nodes;
	}

	public void setNodes(List<SnapshotNodeDTO> nodes) {
		this.nodes = nodes;
	}
}
