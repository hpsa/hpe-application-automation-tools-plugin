package com.hp.octane.plugins.jenkins.model.pipeline;

import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.pipeline.utils.AbstractProjectProcessor;
import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import com.hp.octane.plugins.jenkins.model.scm.SCMDataFactory;
import hudson.model.*;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public final class SnapshotItem extends AbstractItem {
	private int number = -1;
	private CIEventCauseBase cause = null;
	private SnapshotStatus status = SnapshotStatus.UNAVAILABLE;
	private SnapshotResult result = SnapshotResult.UNAVAILABLE;
	private long estimatedDuration = -1;
	private long startTime = -1;
	private long duration = -1;
	private SCMData scmData = null;
	private ParameterInstance[] parameters;
	private SnapshotPhase[] internals;
	private SnapshotPhase[] postBuilds;

	@SuppressWarnings("unchecked")
	public SnapshotItem(AbstractBuild build) {
		super(build.getProject().getName());
		AbstractProject project = build.getProject();
		ParametersAction parametersAction;
		ArrayList<ParameterValue> parametersValues;
		ParameterConfig[] parametersConfigs;
		if (build != null) {
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
			parametersAction = build.getAction(ParametersAction.class);
			if (parametersAction != null) {
				parametersValues = new ArrayList<ParameterValue>(parametersAction.getParameters());
			} else {
				parametersValues = new ArrayList<ParameterValue>();
			}
			parametersConfigs = super.getParameterConfigs(project);
			parameters = new ParameterInstance[parametersConfigs.length];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = new ParameterInstance(parametersConfigs[i]);
				for (int j = 0; j < parametersValues.size(); j++) {
					//  TODO: reevaluate config to value mapping logic
					if (parametersValues.get(j).getName().compareTo(parametersConfigs[i].getName()) == 0) {
						parameters[i].setValue(parametersValues.get(j).getValue());
						parametersValues.remove(j);
						break;
					}
				}
			}
			AbstractProjectProcessor flowProcessor = super.getFlowProcessor(project);
			internals = inflatePhases(build, flowProcessor.getInternals());
			postBuilds = inflatePhases(build, flowProcessor.getPostBuilds());
		}
	}

	public SnapshotItem(AbstractProject project) {
		super(project.getName());
		AbstractProjectProcessor flowProcessor = super.getFlowProcessor(project);
		internals = inflatePhases(null, flowProcessor.getInternals());
		postBuilds = inflatePhases(null, flowProcessor.getPostBuilds());
	}

	private SnapshotPhase[] inflatePhases(AbstractBuild self, StructurePhase[] structures) {
		SnapshotPhase[] phases = new SnapshotPhase[structures.length];
		for (int i = 0; i < phases.length; i++) {
			phases[i] = new SnapshotPhase(self, structures[i]);
		}
		return phases;
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

	@Override
	ParameterConfig[] provideParameters() {
		return parameters;
	}

	@Override
	AbstractPhase[] providePhasesInternal() {
		return internals;
	}

	@Override
	AbstractPhase[] providePhasesPostBuilds() {
		return postBuilds;
	}
}
