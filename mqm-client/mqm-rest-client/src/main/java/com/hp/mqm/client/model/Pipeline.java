// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

import java.util.List;

final public class Pipeline {

	final private Long id;
	final private String name;
	final private boolean pipelineRoot;
	final private Long workspaceId;
	final private Long releaseId;
	final private List<Taxonomy> taxonomies;
	final private List<Field> fields;

	public Pipeline(Long id, String name, boolean pipelineRoot, Long workspaceId,  Long releaseId, List<Taxonomy> taxonomies, List<Field> fields) {
		this.id = id;
		this.name = name;
		this.pipelineRoot = pipelineRoot;
		this.workspaceId = workspaceId;
		this.releaseId = releaseId;
		this.taxonomies = taxonomies;
		this.fields = fields;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Long getWorkspaceId() {
		return workspaceId;
	}

	public Long getReleaseId() {
		return releaseId;
	}

	public List<Taxonomy> getTaxonomies() {
		return taxonomies;
	}

	public List<Field> getFields() {
		return fields;
	}

	public boolean isPipelineRoot() {
		return pipelineRoot;
	}
}
