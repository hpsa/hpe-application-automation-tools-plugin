package com.hp.nga.integrations.dto.scm;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */

public class SCMRepository {
	private final SCMType type;
	private final String url;
	private final String branch;

	public SCMRepository(SCMType type, String url, String branch) {
		this.type = type;
		this.url = url;
		this.branch = branch;
	}

	public String getType() {
		return type.toString();
	}

	public String getUrl() {
		return url;
	}

	public String getBranch() {
		return branch;
	}
}
