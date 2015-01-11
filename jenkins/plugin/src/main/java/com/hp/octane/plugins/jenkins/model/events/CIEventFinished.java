package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.model.pipeline.SnapshotResult;
import com.hp.octane.plugins.jenkins.model.CIServerType;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:50
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class CIEventFinished extends CIEventBase {
	public final CIEventType eventType = CIEventType.FINISHED;
	public int number;
	public SnapshotResult result;
	public long duration;
	public SCMData scmData;

	public CIEventFinished(CIServerType serverType, String serverURL, String project, int number, SnapshotResult result, long duration, SCMData scmData, CIEventCauseBase cause) {
		super(serverType, serverURL, project, cause);
		this.number = number;
		this.result = result;
		this.duration = duration;
		this.scmData = scmData;
	}

	@Override
	public CIEventType getEventType() {
		return eventType;
	}
}
