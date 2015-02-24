package com.hp.octane.plugins.jenkins.model.causes;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:44
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class CIEventUpstreamCause implements CIEventCauseBase {
	private String project;
	private int number;
	private CIEventCauseBase[] causes;

	public CIEventUpstreamCause(String project, int number, CIEventCauseBase[] causes) {
		this.project = project;
		this.number = number;
		this.causes = causes;
	}

	@Override
	@Exported(inline = true)
	public CIEventCauseType getType() {
		return CIEventCauseType.UPSTREAM;
	}

	@Exported(inline = true)
	public String getProject() {
		return project;
	}

	@Exported(inline = true)
	public int getNumber() {
		return number;
	}

	@Exported(inline = true)
	public CIEventCauseBase[] getCauses() {
		return causes;
	}
}
