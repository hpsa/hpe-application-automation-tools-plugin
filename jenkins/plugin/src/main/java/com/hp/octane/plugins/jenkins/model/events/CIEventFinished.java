package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.model.pipeline.SnapshotResult;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:50
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public final class CIEventFinished extends CIEventBase {
	private final CIEventType eventType = CIEventType.FINISHED;
	private int number;
	private SnapshotResult result;
	private long duration;
	private SCMData scmData;

	public CIEventFinished(String serverURL, String project, int number, SnapshotResult result, long duration, SCMData scmData, CIEventCauseBase cause) {
		super(serverURL, project, cause);
		this.number = number;
		this.result = result;
		this.duration = duration;
		this.scmData = scmData;
	}

	@Override
	CIEventType provideEventType() {
		return eventType;
	}

	@Exported(inline = true)
	public int getNumber() {
		return number;
	}

	@Exported(inline = true)
	public String getResult() {
		return result.toString();
	}

	@Exported(inline = true)
	public long getDuration() {
		return duration;
	}

	@Exported(inline = true)
	public SCMData getScmData() {
		return scmData;
	}
}
