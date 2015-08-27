// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

import java.util.List;

final public class Pipeline {

	private long id;
	private String name;
	private Boolean root;
	private long workspaceId;
	private Long releaseId;
	private String releaseName;
	private List<Taxonomy> taxonomies;
	private List<ListField> fields;

	public Pipeline(long id, String name, Boolean root, long workspaceId, Long releaseId, String releaseName, List<Taxonomy> taxonomies, List<ListField> fields) {
		this.id = id;
		this.name = name;
		this.root = root;
		this.workspaceId = workspaceId;
		this.releaseId = releaseId;
		this.releaseName = releaseName;
		this.taxonomies = taxonomies;
		this.fields = fields;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getWorkspaceId() {
		return workspaceId;
	}

	public Long getReleaseId() {
		return releaseId;
	}

	public void setReleaseId(Long releaseId) {
		this.releaseId = releaseId;
	}

	public List<Taxonomy> getTaxonomies() {
		return taxonomies;
	}

	public void setTaxonomies(List<Taxonomy> taxonomies) {
		this.taxonomies = taxonomies;
	}

	public List<ListField> getFields() {
		return fields;
	}

	public Boolean isRoot() {
		return root;
	}

	public String getReleaseName() {
		return releaseName;
	}

	public void setReleaseName(final String releaseName) {
		this.releaseName = releaseName;
	}
}
