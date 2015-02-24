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
	public CIEventQueued(String project, CIEventCauseBase[] causes) {
		super(project, causes);
	}

	@Override
	CIEventType provideEventType() {
		return CIEventType.QUEUED;
	}
}
