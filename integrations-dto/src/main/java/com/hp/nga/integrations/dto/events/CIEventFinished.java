package com.hp.nga.integrations.dto.events;

import com.hp.nga.integrations.dto.causes.CIEventCauseBase;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.nga.integrations.dto.snapshots.SnapshotResult;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:50
 * To change this template use File | Settings | File Templates.
 */

public final class CIEventFinished extends CIEventStarted {
	private SnapshotResult result;
	private long duration;
	private SCMData scmData;

	public CIEventFinished(
			String project,
			int number,
			int subNumber,
			long startTime,
			long estimatedDuration,
			CIEventCauseBase[] causes,
			ParameterInstance[] parameters,
			SnapshotResult result,
			long duration,
			SCMData scmData) {
		super(project, number, subNumber, startTime, estimatedDuration, causes, parameters);
		this.result = result;
		this.duration = duration;
		this.scmData = scmData;
	}

	@Override
	public CIEventType getEventType() {
		return CIEventType.FINISHED;
	}

	public SnapshotResult getResult() {
		return result;
	}

	public long getDuration() {
		return duration;
	}

	public SCMData getScmData() {
		return scmData;
	}
}
