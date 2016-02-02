package com.hp.nga.integrations.dto.events;

import com.hp.nga.integrations.dto.causes.CIEventCauseBase;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */

public abstract class CIEventBase {
	private String project;
	private CIEventCauseBase[] causes;

	public CIEventBase(String project, CIEventCauseBase[] causes) {
		this.project = project;
		this.causes = causes;
	}

	public abstract CIEventType getEventType();

	public String getProject() {
		return project;
	}

	public CIEventCauseBase[] getCauses() {
		return causes;
	}
}