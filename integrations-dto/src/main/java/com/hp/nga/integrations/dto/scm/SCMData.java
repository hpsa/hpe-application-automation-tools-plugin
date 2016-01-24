package com.hp.nga.integrations.dto.scm;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/10/14
 * Time: 22:33
 * To change this template use File | Settings | File Templates.
 */

public class SCMData {
	private final SCMRepository repository;
	private final String builtRevId;
	private final SCMCommit[] commits;

	public SCMData(SCMRepository repository, String builtRevId, SCMCommit[] commits) {
		this.repository = repository;
		this.builtRevId = builtRevId;
		this.commits = commits;
	}

	public SCMRepository getRepository() {
		return repository;
	}

	public String getBuiltRevId() {
		return builtRevId;
	}

	public SCMCommit[] getCommits() {
		return commits;
	}
}
