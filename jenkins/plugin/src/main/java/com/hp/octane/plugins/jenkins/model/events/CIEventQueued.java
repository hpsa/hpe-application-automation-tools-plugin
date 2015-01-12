package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public final class CIEventQueued extends CIEventBase {
	private final CIEventType eventType = CIEventType.QUEUED;

	public CIEventQueued(String serverURL, String project, CIEventCauseBase cause) {
		super(serverURL, project, cause);
	}

	@Override
	CIEventType provideEventType() {
		return eventType;
	}
}
