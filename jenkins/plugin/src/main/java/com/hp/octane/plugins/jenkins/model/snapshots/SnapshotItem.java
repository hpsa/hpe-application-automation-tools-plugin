package com.hp.octane.plugins.jenkins.model.snapshots;

import com.hp.nga.integrations.dto.snapshots.SnapshotResult;
import com.hp.nga.integrations.dto.snapshots.SnapshotStatus;
import com.hp.octane.plugins.jenkins.model.api.AbstractItem;
import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.pipelines.StructureItem;
import com.hp.octane.plugins.jenkins.model.pipelines.StructurePhase;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public final class SnapshotItem extends AbstractItem<ParameterInstance, SnapshotPhase> {
	private static final Logger logger = Logger.getLogger(SnapshotItem.class.getName());

	private Integer number = null;
	private CIEventCauseBase[] causes = null;
	private SnapshotStatus status = SnapshotStatus.UNAVAILABLE;
	private SnapshotResult result = SnapshotResult.UNAVAILABLE;
	private Long estimatedDuration = null;
	private Long startTime = null;
	private Long duration = null;
	private SCMData scmData = null;

	@SuppressWarnings("unchecked")
	public SnapshotItem(AbstractBuild build, boolean metaOnly) {
		super(build.getProject());

		SCMProcessor scmProcessor = SCMProcessors.getAppropriate(build.getProject().getScm().getClass().getName());
		number = build.getNumber();
		causes = CIEventCausesFactory.processCauses(build.getCauses());
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
		startTime = timeInUTC(build.getStartTimeInMillis());
		duration = build.getDuration();
		scmData = scmProcessor == null ? null : scmProcessor.getSCMData(build);

		setParameters(ParameterProcessors.getInstances(build));

		if (!metaOnly) {
			StructurePhase[] tmpStructurePhasesInternals = super.getFlowProcessor().getInternals();
			StructurePhase[] tmpStructurePhasesPostBuilds = super.getFlowProcessor().getPostBuilds();
			ArrayList<String> invokeesNames = new ArrayList<String>();
			appendInvokeesNames(invokeesNames, tmpStructurePhasesInternals);
			appendInvokeesNames(invokeesNames, tmpStructurePhasesPostBuilds);
			HashMap<String, ArrayList<AbstractBuild>> invokedBuilds = getInvokedBuilds(build, invokeesNames);
			setInternals(inflatePhases(tmpStructurePhasesInternals, invokedBuilds));
			setPostBuilds(inflatePhases(tmpStructurePhasesPostBuilds, invokedBuilds));
		}
	}

	public SnapshotItem(AbstractProject project, boolean metaOnly) {
		super(project);
		if (!metaOnly) {
			setInternals(inflatePhases(super.getFlowProcessor().getInternals(), null));
			setPostBuilds(inflatePhases(super.getFlowProcessor().getPostBuilds(), null));
		}
	}

	@Exported(inline = true)
	public Integer getNumber() {
		return number;
	}

	@Exported(inline = true)
	public CIEventCauseBase[] getCauses() {
		return causes;
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
	public Long getEstimatedDuration() {
		return estimatedDuration;
	}

	@Exported(inline = true)
	public Long getStartTime() {
		return startTime;
	}

	@Exported(inline = true)
	public Long getDuration() {
		return duration;
	}

	@Exported(inline = true)
	public SCMData getScmData() {
		return scmData;
	}

	private void appendInvokeesNames(ArrayList<String> list, StructurePhase[] phases) {
		for (StructurePhase phase : phases) {
			for (StructureItem item : phase.getJobs()) {
				if (item != null) {
					if (!list.contains(item.getName())) list.add(item.getName());
				} else {
					logger.severe("null referenced project encountered; considering it as corrupted configuration and skipping");
				}
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

	//  TODO: replace with the real method from Utils class
	private long timeInUTC(long input) {
		return input;
		//return input - Calendar.getInstance().getTimeZone().getRawOffset();
	}
}
