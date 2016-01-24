package com.hp.octane.plugins.jetbrains.teamcity.model.causes;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:44
 * To change this template use File | Settings | File Templates.
 */

public class CIEventUpstreamCause implements CIEventCauseBase {
	private CIEventCauseType type = CIEventCauseType.UPSTREAM;

	private String project;
	private int number;
	private CIEventCauseBase[] causes;

	public CIEventUpstreamCause(String project, int number, CIEventCauseBase[] causes) {
		this.project = project;
		this.number = number;
		this.causes = causes;
	}

	@Override
	public CIEventCauseType getType() {
		return type;
	}

	public String getProject() {
		return project;
	}

	public int getNumber() {
		return number;
	}

	public CIEventCauseBase[] getCauses() {
		return causes;
	}
}
