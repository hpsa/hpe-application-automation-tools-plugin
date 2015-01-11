package com.hp.octane.plugins.jenkins.model.pipeline;

import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import com.hp.octane.plugins.jenkins.model.scm.SCMDataFactory;
import hudson.model.*;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class SnapshotItem extends FlowItem {
	private int number = -1;
	private CIEventCauseBase cause = null;
	private SnapshotStatus status = SnapshotStatus.UNAVAILABLE;
	private SnapshotResult result = SnapshotResult.UNAVAILABLE;
	private long estimatedDuration = -1;
	private long startTime = -1;
	private long duration = -1;
	private SCMData scmData = null;

	public SnapshotItem(AbstractBuild build) {
		this(build, build.getProject());
	}

	public SnapshotItem(AbstractBuild build, AbstractProject project) {
		super(project);
		ParametersAction parametersAction;
		if (build != null) {
			parametersAction = build.getAction(ParametersAction.class);
			if (parametersAction != null) {
				for (ParameterValue v : parametersAction.getParameters()) {
					for (int i = 0; i < parameters.length; i++) {
						if (parameters[i] instanceof ParameterInstance) continue;
						//  TODO: reevaluate config to value mapping logic
						if (parameters[i].getName().compareTo(v.getName()) == 0) {
							parameters[i] = new ParameterInstance(v, parameters[i]);
							break;
						}
					}
				}
			}
			number = build.getNumber();
			cause = CIEventCausesFactory.convertCause(build.getCauses());
			if (build.hasntStartedYet()) {
				status = SnapshotStatus.QUEUED;
			} else if (build.isBuilding()) {
				status = SnapshotStatus.RUNNING;
			} else {
				status = SnapshotStatus.FINISHED;
			}
			if (build.getResult() == Result.SUCCESS) {
				result = SnapshotResult.SUCCESS;
			} else if (build.getResult() == Result.ABORTED) {
				result = SnapshotResult.ABORTED;
			} else if (build.getResult() == Result.FAILURE) {
				result = SnapshotResult.FAILURE;
			} else if (build.getResult() == Result.UNSTABLE) {
				result = SnapshotResult.UNSTABLE;
			}
			estimatedDuration = build.getEstimatedDuration();
			startTime = build.getStartTimeInMillis();
			duration = build.getDuration();
			scmData = SCMDataFactory.create(build);
		}
	}

	@Exported(inline = true)
	public int getNumber() {
		return number;
	}

	@Exported(inline = true)
	public CIEventCauseBase getCause() {
		return cause;
	}

	@Exported(inline = true)
	public SnapshotStatus getStatus() {
		return status;
	}

	@Exported(inline = true)
	public SnapshotResult getResult() {
		return result;
	}

	@Exported(inline = true)
	public long getEstimatedDuration() {
		return estimatedDuration;
	}

	@Exported(inline = true)
	public long getStartTime() {
		return startTime;
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
