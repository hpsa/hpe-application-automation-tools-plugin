package com.hp.octane.plugins.jenkins.model.scm;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class SCMRepositoryData {
	private SCMType type;
	private String uri;
	private String builtBranch;
	private String builtCommitRev;
	private ArrayList<SCMCommit> commits;

	public SCMRepositoryData(SCMType type, String uri) {
		this.type = type;
		this.uri = uri;
		this.builtBranch = null;
		this.builtCommitRev = null;
		this.commits = null;
	}

	public SCMRepositoryData(SCMType type, String uri, String builtCommitRev, String builtBranch) {
		this.type = type;
		this.uri = uri;
		this.builtBranch = builtBranch;
		this.builtCommitRev = builtCommitRev;
		this.commits = new ArrayList<SCMCommit>();
	}

	public void addCommit(SCMCommit commit) {
		commits.add(commit);
	}

	@Exported(inline = true)
	public String getType() {
		return type.toString();
	}

	@Exported(inline = true)
	public String getUri() {
		return uri;
	}

	@Exported(inline = true)
	public String getBuiltBranch() {
		return builtBranch;
	}

	@Exported(inline = true)
	public String getBuiltCommitRev() {
		return builtCommitRev;
	}

	@Exported(inline = true)
	public SCMCommit[] getCommits() {
		return commits.toArray(new SCMCommit[commits.size()]);
	}
}
