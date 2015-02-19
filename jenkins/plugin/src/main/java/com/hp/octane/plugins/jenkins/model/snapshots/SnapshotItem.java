package com.hp.octane.plugins.jenkins.model.snapshots;

import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.pipelines.*;
import com.hp.octane.plugins.jenkins.model.processors.parameters.AbstractParametersProcessor;
import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import com.hp.octane.plugins.jenkins.model.scm.SCMDataFactory;
import com.hp.octane.plugins.jenkins.model.api.AbstractItem;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public final class SnapshotItem extends AbstractItem<ParameterInstance, SnapshotPhase> {
	private int number = -1;
	private CIEventCauseBase cause = null;
	private SnapshotStatus status = SnapshotStatus.UNAVAILABLE;
	private SnapshotResult result = SnapshotResult.UNAVAILABLE;
	private long estimatedDuration = -1;
	private long startTime = -1;
	private long duration = -1;
	private SCMData scmData = null;

	@SuppressWarnings("unchecked")
	public SnapshotItem(AbstractBuild build) {
		super(build.getProject());

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

		setParameters(AbstractParametersProcessor.getInstances(build));

		StructurePhase[] tmpStructurePhasesInternals = super.getFlowProcessor().getInternals();
		StructurePhase[] tmpStructurePhasesPostBuilds = super.getFlowProcessor().getPostBuilds();
		ArrayList<String> invokeesNames = new ArrayList<String>();
		appendInvokeesNames(invokeesNames, tmpStructurePhasesInternals);
		appendInvokeesNames(invokeesNames, tmpStructurePhasesPostBuilds);
		HashMap<String, ArrayList<AbstractBuild>> invokedBuilds = getInvokedBuilds(build, invokeesNames);

		setInternals(inflatePhases(tmpStructurePhasesInternals, invokedBuilds));
		setPostBuilds(inflatePhases(tmpStructurePhasesPostBuilds, invokedBuilds));
	}

	public SnapshotItem(AbstractProject project) {
		super(project);
		setInternals(inflatePhases(super.getFlowProcessor().getInternals(), null));
		setPostBuilds(inflatePhases(super.getFlowProcessor().getPostBuilds(), null));
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

	private void appendInvokeesNames(ArrayList<String> list, StructurePhase[] phases) {
		for (StructurePhase phase : phases) {
			for (StructureItem item : phase.getItems()) {
				if (!list.contains(item.getName())) list.add(item.getName());
			}
		}
	}

	private HashMap<String, ArrayList<AbstractBuild>> getInvokedBuilds(AbstractBuild self, ArrayList<String> invokeesNames) {
		HashMap<String, ArrayList<AbstractBuild>> result = new HashMap<String, ArrayList<AbstractBuild>>();
		AbstractProject project;
		for (String invokeeName : invokeesNames) {
			project = (AbstractProject) Jenkins.getInstance().getItem(invokeeName);
			result.put(invokeeName, getInvokees(self, project));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<AbstractBuild> getInvokees(AbstractBuild invoker, AbstractProject project) {
		ArrayList<AbstractBuild> result = new ArrayList<AbstractBuild>();
		AbstractBuild tmpBuild;
		Cause.UpstreamCause tmpCause;
		for (Object o : project.getBuilds()) {
			tmpBuild = (AbstractBuild) o;
			for (Cause cause : (List<Cause>) tmpBuild.getCauses()) {
				if (!(cause instanceof Cause.UpstreamCause)) continue;

				tmpCause = (Cause.UpstreamCause) cause;
				if (tmpCause.pointsTo(invoker)) {
					result.add(0, tmpBuild);
				} else if (tmpCause.pointsTo(invoker.getProject()) && tmpCause.getUpstreamBuild() < invoker.getNumber()) {
					return result;
				}
			}
		}
		return result;
	}

	private SnapshotPhase[] inflatePhases(StructurePhase[] structures, HashMap<String, ArrayList<AbstractBuild>> invokedBuilds) {
		SnapshotPhase[] phases = new SnapshotPhase[structures.length];
		for (int i = 0; i < phases.length; i++) {
			phases[i] = new SnapshotPhase(structures[i], invokedBuilds);
		}
		return phases;
	}
}
