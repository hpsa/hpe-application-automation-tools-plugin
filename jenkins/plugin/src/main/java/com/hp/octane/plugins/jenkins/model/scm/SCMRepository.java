package com.hp.octane.plugins.jenkins.model.scm;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */
public class SCMRepository {
	private SCMType type;
	private String uri;
	private String builtBranch;
	private String builtCommitRev;
	private ArrayList<SCMCommit> commits;

	public SCMRepository(SCMType type, String uri, String builtCommitRev, String builtBranch) {
		this.type = type;
		this.uri = uri;
		this.builtBranch = builtBranch;
		this.builtCommitRev = builtCommitRev;
		this.commits = new ArrayList<SCMCommit>();
	}

	public SCMRepository(SCMType type, String uri) {
		this.type = type;
		this.uri = uri;
		this.builtBranch = null;
		this.builtCommitRev = null;
		this.commits = new ArrayList<SCMCommit>();
	}

	public void addCommit(SCMCommit commit) {
		commits.add(commit);
	}

	public SCMType getType() {
		return type;
	}

	public String getUri() {
		return uri;
	}

	public String getBuiltCommitRev() {
		return builtCommitRev;
	}

	public String getBuiltBranch() {
		return builtBranch;
	}
}
