package com.hp.nga.dto.builds;

import java.util.List;

/**
 * Created by gullery on 15/01/2016.
 */

public class SnapshotDTO {
	private List<SnapshotNodeDTO> nodes;
	private List<SnapshotNodesChainDTO> chains;

	public List<SnapshotNodeDTO> getNodes() {
		return nodes;
	}

	public void setNodes(List<SnapshotNodeDTO> nodes) {
		this.nodes = nodes;
	}

	public List<SnapshotNodesChainDTO> getChains() {
		return chains;
	}

	public void setChains(List<SnapshotNodesChainDTO> chains) {
		this.chains = chains;
	}
}
