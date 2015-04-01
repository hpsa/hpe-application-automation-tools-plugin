package com.hp.octane.plugins.jenkins.model.scm;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created by gullery on 01/04/2015.
 */

@ExportedBean
public class SCMConfiguration {
	private SCMType type;
	private String uri;
	private String builtBranch;
	private String builtCommitRev;

	public SCMConfiguration(SCMType type, String uri, String builtBranch, String builtCommitRev) {
		this.type = type;
		this.uri = uri;
		this.builtBranch = builtBranch;
		this.builtCommitRev = builtCommitRev;
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
}
