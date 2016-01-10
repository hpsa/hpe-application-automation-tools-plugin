package com.hp.octane.plugins.jenkins.events;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import com.hp.octane.plugins.jenkins.model.snapshots.SnapshotResult;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.events.CIEventFinished;
import com.hp.octane.plugins.jenkins.model.events.CIEventStarted;
import com.hp.octane.plugins.jenkins.tests.TestListener;
import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.*;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/08/14
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */

@Extension
public final class RunListenerImpl extends RunListener<Run> {
	private static Logger logger = Logger.getLogger(RunListenerImpl.class.getName());

	@Inject
	private TestListener testListener;

	@Override
	public void onStarted(Run r, TaskListener listener) {
		CIEventStarted event;
		if (r.getParent() instanceof MatrixConfiguration) {
			AbstractBuild build = (AbstractBuild) r;
			event = new CIEventStarted(
					((MatrixRun) r).getParentBuild().getParent().getName(),
					((MatrixRun) r).getParentBuild().getNumber(),
					build.getNumber(),
					build.getStartTimeInMillis(),
					build.getEstimatedDuration(),
					CIEventCausesFactory.processCauses(extractCauses(build)),
					ParameterProcessors.getInstances(build)
			);
			EventsService.getExtensionInstance().dispatchEvent(event);
		} else if (r instanceof AbstractBuild) {
			AbstractBuild build = (AbstractBuild) r;
			event = new CIEventStarted(
					build.getProject().getName(),
					build.getNumber(),
					-1,
					build.getStartTimeInMillis(),
					build.getEstimatedDuration(),
					CIEventCausesFactory.processCauses(extractCauses(build)),
					ParameterProcessors.getInstances(build)
			);
			EventsService.getExtensionInstance().dispatchEvent(event);
		}
	}

	@Override
	public void onCompleted(Run r, @Nonnull TaskListener listener) {
		if (r instanceof AbstractBuild) {
			AbstractBuild build = (AbstractBuild) r;
			SnapshotResult result;
			if (build.getResult() == Result.SUCCESS) {
				result = SnapshotResult.SUCCESS;
			} else if (build.getResult() == Result.ABORTED) {
				result = SnapshotResult.ABORTED;
			} else if (build.getResult() == Result.FAILURE) {
				result = SnapshotResult.FAILURE;
			} else if (build.getResult() == Result.UNSTABLE) {
				result = SnapshotResult.UNSTABLE;
			} else {
				result = SnapshotResult.UNAVAILABLE;
			}

			SCMProcessor scmProcessor = SCMProcessors.getAppropriate(build.getProject().getScm().getClass().getName());
			CIEventFinished event = new CIEventFinished(
					getProjectName(r),
					build.getNumber(),
					-1,
					build.getStartTimeInMillis(),
					build.getEstimatedDuration(),
					CIEventCausesFactory.processCauses(extractCauses(build)),
					ParameterProcessors.getInstances(build),
					result,
					build.getDuration(),
					scmProcessor == null ? null : scmProcessor.getSCMData(build)
			);
			EventsService.getExtensionInstance().dispatchEvent(event);

			testListener.processBuild(build);
		}
	}

	private String getProjectName(Run r) {
		if (r.getParent() instanceof MatrixConfiguration) {
			return ((MatrixRun) r).getParentBuild().getParent().getName();
		}
		return ((AbstractBuild) r).getProject().getName();
	}

	private List<? extends Cause> extractCauses(Run r) {
		if (r.getParent() instanceof MatrixConfiguration) {
			return ((MatrixRun) r).getParentBuild().getCauses();
		} else {
			return r.getCauses();
		}
	}
}
