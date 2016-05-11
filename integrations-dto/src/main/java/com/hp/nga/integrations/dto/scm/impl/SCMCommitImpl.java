package com.hp.nga.integrations.dto.scm.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.scm.SCMChange;
import com.hp.nga.integrations.dto.scm.SCMCommit;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:51
 * To change this template use File | Settings | File Templates.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class SCMCommitImpl implements SCMCommit {
	private Long time;
	private String user;
	private String revId;
	private String parentRevId;
	private String comment;
	private List<SCMChange> changes;

	public Long getTime() {
		return time;
	}

	public SCMCommit setTime(Long time) {
		this.time = time;
		return this;
	}

	public String getUser() {
		return user;
	}

	public SCMCommit setUser(String user) {
		this.user = user;
		return this;
	}

	public String getRevId() {
		return revId;
	}

	public SCMCommit setRevId(String revId) {
		this.revId = revId;
		return this;
	}

	public String getParentRevId() {
		return parentRevId;
	}

	public SCMCommit setParentRevId(String parentRevId) {
		this.parentRevId = parentRevId;
		return this;
	}

	public String getComment() {
		return comment;
	}

	public SCMCommit setComment(String comment) {
		this.comment = comment;
		return this;
	}

	public List<SCMChange> getChanges() {
		return changes;
	}

	public SCMCommit setChanges(List<SCMChange> changes) {
		this.changes = changes;
		return this;
	}
}
