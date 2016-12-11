package com.hp.octane.integrations.dto.scm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.scm.SCMRepository;
import com.hp.octane.integrations.dto.scm.SCMType;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class SCMRepositoryImpl implements SCMRepository {
	private SCMType type;
	private String url;
	private String branch;

	public SCMType getType() {
		return type;
	}

	public SCMRepository setType(SCMType type) {
		this.type = type;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public SCMRepository setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getBranch() {
		return branch;
	}

	public SCMRepository setBranch(String branch) {
		this.branch = branch;
		return this;
	}
}
