package com.hp.nga.integrations.dto.events;

import com.hp.nga.integrations.dto.causes.CIEventCause;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */

public class CIEventQueued extends CIEventBase {
	public CIEventQueued(String project, CIEventCause[] causes) {
		super(project, causes);
	}

	@Override
	public CIEventType getEventType() {
		return CIEventType.QUEUED;
	}
}
