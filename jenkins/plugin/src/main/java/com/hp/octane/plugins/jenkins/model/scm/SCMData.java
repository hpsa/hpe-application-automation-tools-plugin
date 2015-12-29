package com.hp.octane.plugins.jenkins.model.scm;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/10/14
 * Time: 22:33
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class SCMData {
	private final SCMRepository repository;
	private final String builtRevId;
	private final SCMCommit[] commits;

	public SCMData(SCMRepository repository, String builtRevId, SCMCommit[] commits) {
		this.repository = repository;
		this.builtRevId = builtRevId;
		this.commits = commits;
	}

	@Exported(inline = true)
	public SCMRepository getRepository() {
		return repository;
	}

	@Exported(inline = true)
	public String getBuiltRevId() {
		return builtRevId;
	}

	@Exported(inline = true)
	public SCMCommit[] getCommits() {
		return commits;
	}
}
