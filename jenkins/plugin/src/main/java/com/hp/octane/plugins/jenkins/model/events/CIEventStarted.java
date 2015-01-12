package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public final class CIEventStarted extends CIEventBase {
	private final CIEventType eventType = CIEventType.STARTED;
	private int number;
	private long startTime;
	private long estimatedDuration;

	public CIEventStarted(String serverURL, String project, int number, long startTime, long estimatedDuration, CIEventCauseBase cause) {
		super(serverURL, project, cause);
		this.number = number;
		this.startTime = startTime;
		this.estimatedDuration = estimatedDuration;
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
	public long getStartTime() {
		return startTime;
	}

	@Exported(inline = true)
	public long getEstimatedDuration() {
		return estimatedDuration;
	}
}
