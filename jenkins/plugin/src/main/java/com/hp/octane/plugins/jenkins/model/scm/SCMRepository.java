package com.hp.octane.plugins.jenkins.model.scm;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class SCMRepository {
	private final SCMType type;
	private final String url;
	private final String branch;

	public SCMRepository(SCMType type, String url, String branch) {
		this.type = type;
		this.url = url;
		this.branch = branch;
	}

	@Exported(inline = true)
	public String getType() {
		return type.toString();
	}

	@Exported(inline = true)
	public String getUrl() {
		return url;
	}

	@Exported(inline = true)
	public String getBranch() {
		return branch;
	}
}
