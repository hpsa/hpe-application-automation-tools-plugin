package com.hp.octane.plugins.jenkins.model.events;

import com.hp.nga.integrations.dto.causes.CIEventCauseBase;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.nga.integrations.dto.snapshots.SnapshotResult;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;
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
	CIEventType provideEventType() {
		return CIEventType.FINISHED;
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
