package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.model.CIServerType;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class CIEventQueued extends CIEventBase {
	public final CIEventType eventType = CIEventType.QUEUED;

	public CIEventQueued(CIServerType serverType, String serverURL, String project, CIEventCauseBase cause) {
		super(serverType, serverURL, project, cause);
	}

	@Override
	public CIEventType getEventType() {
		return eventType;
	}
}
