// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

import java.util.List;

final public class Pipeline {

	private long id;
	private String name;
	private Boolean root;
	private Long releaseId;
	private List<Taxonomy> taxonomies;
	private List<Field> fields;

	public Pipeline(long id, String name, Boolean root, Long releaseId, List<Taxonomy> taxonomies, List<Field> fields) {
		this.id = id;
		this.name = name;
		this.root = root;
		this.releaseId = releaseId;
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

	public List<Field> getFields() {
		return fields;
	}

	public Boolean isRoot() {
		return root;
	}
}
