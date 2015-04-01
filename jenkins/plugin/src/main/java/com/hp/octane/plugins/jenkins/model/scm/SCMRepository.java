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
public class SCMRepository {
	private SCMConfiguration configuration;
	private SCMCommit[] commits;

	public SCMRepository(SCMConfiguration configuration, SCMCommit[] commits) {
		this.configuration = configuration;
		this.commits = commits == null ? new SCMCommit[0] : commits.clone();
	}

	@Exported(inline = true)
	public SCMConfiguration getConfiguration() {
		return configuration;
	}

	@Exported(inline = true)
	public SCMCommit[] getCommits() {
		return commits.clone();
	}
}
