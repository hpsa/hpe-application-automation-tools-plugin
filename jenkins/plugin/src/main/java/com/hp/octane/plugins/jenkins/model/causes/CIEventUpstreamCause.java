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

//  TODO: add support for multiple causes

@ExportedBean
public class CIEventUpstreamCause implements CIEventCauseBase {
	private final CIEventCauseType type = CIEventCauseType.UPSTREAM;
	private String project;
	private int number;
	private CIEventCauseBase cause;

	public CIEventUpstreamCause(String project, int number, CIEventCauseBase cause) {
		this.project = project;
		this.number = number;
		this.cause = cause;
	}

	@Override
	@Exported(inline = true)
	public CIEventCauseType getType() {
		return type;
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
	public CIEventCauseBase getCause() {
		return cause;
	}
}
