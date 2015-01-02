package com.hp.octane.plugins.jenkins.notifications;

import com.hp.devops.pipelines.SnapshotResult;
import com.hp.mqm.plugins.jenkins.commons.CIEventCausesFactory;
import com.hp.mqm.plugins.jenkins.scm.SCMDataFactory;
import com.hp.devops.providers.CIServerType;
import com.hp.devops.providers.events.CIEventFinished;
import com.hp.devops.providers.events.CIEventStarted;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/08/14
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
@Extension
public final class RunListenerImpl extends RunListener<Run> {

	@Override
	public void onStarted(Run r, TaskListener listener) {
		if (r instanceof AbstractBuild) {
			AbstractBuild build = (AbstractBuild) r;
			CIEventStarted event = new CIEventStarted(
					CIServerType.JENKINS,
					EventDispatcher.SELF_URL,
					build.getProject().getName(),
					build.getNumber(),
					build.getStartTimeInMillis(),
					build.getEstimatedDuration(),
					CIEventCausesFactory.convertCause(build.getCauses())
			);
			EventDispatcher.dispatchEvent(event);
		}
	}

	@Override
	public void onCompleted(Run r, TaskListener listener) {
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
			CIEventFinished event = new CIEventFinished(
					CIServerType.JENKINS,
					EventDispatcher.SELF_URL,
					build.getProject().getName(),
					build.getNumber(),
					result,
					build.getDuration(),
					SCMDataFactory.create(build),
					CIEventCausesFactory.convertCause(build.getCauses())
			);
			EventDispatcher.dispatchEvent(event);
		}
	}
}
