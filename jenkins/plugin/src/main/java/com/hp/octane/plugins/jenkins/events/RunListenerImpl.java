package com.hp.octane.plugins.jenkins.events;

import com.google.inject.Inject;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.events.CIEvent;
import com.hp.nga.integrations.dto.events.CIEventType;
import com.hp.nga.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.plugins.jenkins.model.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import com.hp.octane.plugins.jenkins.tests.TestListener;
import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.*;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/08/14
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */

@Extension
public final class RunListenerImpl extends RunListener<Run> {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Inject
	private TestListener testListener;

	@Override
	public void onStarted(Run r, TaskListener listener) {
		CIEvent event;
		if (r.getParent() instanceof MatrixConfiguration) {
			AbstractBuild build = (AbstractBuild) r;
			event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.STARTED)
					.setProject(((MatrixRun) r).getParentBuild().getParent().getName())
					.setNumber(String.valueOf(build.getNumber()))
					.setStartTime(build.getStartTimeInMillis())
					.setEstimatedDuration(build.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)))
					.setParameters(ParameterProcessors.getInstances(build));
			EventsService.getExtensionInstance().dispatchEvent(event);
		} else if (r instanceof AbstractBuild) {
			AbstractBuild build = (AbstractBuild) r;
			event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.STARTED)
					.setProject(build.getProject().getName())
					.setNumber(String.valueOf(build.getNumber()))
					.setStartTime(build.getStartTimeInMillis())
					.setEstimatedDuration(build.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)))
					.setParameters(ParameterProcessors.getInstances(build));
			EventsService.getExtensionInstance().dispatchEvent(event);
		}
	}

	@Override
	public void onCompleted(Run r, @Nonnull TaskListener listener) {
		if (r instanceof AbstractBuild) {
			AbstractBuild build = (AbstractBuild) r;
			CIBuildResult result;
			if (build.getResult() == Result.SUCCESS) {
				result = CIBuildResult.SUCCESS;
			} else if (build.getResult() == Result.ABORTED) {
				result = CIBuildResult.ABORTED;
			} else if (build.getResult() == Result.FAILURE) {
				result = CIBuildResult.FAILURE;
			} else if (build.getResult() == Result.UNSTABLE) {
				result = CIBuildResult.UNSTABLE;
			} else {
				result = CIBuildResult.UNAVAILABLE;
			}

			SCMProcessor scmProcessor = SCMProcessors.getAppropriate(build.getProject().getScm().getClass().getName());
			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.FINISHED)
					.setProject(getProjectName(r))
					.setNumber(String.valueOf(build.getNumber()))
					.setStartTime(build.getStartTimeInMillis())
					.setEstimatedDuration(build.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)))
					.setParameters(ParameterProcessors.getInstances(build))
					.setResult(result)
					.setDuration(build.getDuration())
					.setScmData(scmProcessor == null ? null : scmProcessor.getSCMData(build));
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
