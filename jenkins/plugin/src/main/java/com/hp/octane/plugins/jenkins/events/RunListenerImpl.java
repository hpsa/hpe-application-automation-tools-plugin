package com.hp.octane.plugins.jenkins.events;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.causes.CIEventCause;
import com.hp.nga.integrations.dto.events.CIEvent;
import com.hp.nga.integrations.dto.events.CIEventType;
import com.hp.nga.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.plugins.jenkins.model.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import com.hp.octane.plugins.jenkins.tests.TestListener;
import com.hp.octane.plugins.jenkins.tests.gherkin.GherkinEventsService;
import com.hp.octane.plugins.jenkins.workflow.BuildRelations;
import com.hp.octane.plugins.jenkins.workflow.WorkflowGraphListener;
import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

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
	private ExecutorService executor = new ThreadPoolExecutor(0, 5, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	@Inject
	private TestListener testListener;
	
	@Override
	public void onStarted(final Run r, TaskListener listener) {
		CIEvent event;
		if (r instanceof WorkflowRun) {
			final WorkflowRun build = (WorkflowRun) r;
			event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.STARTED)
					.setProject(((WorkflowRun) r).getParent().getName())
					.setBuildCiId(String.valueOf(build.getNumber()))
					.setNumber(String.valueOf(build.getNumber()))
					.setStartTime(build.getStartTimeInMillis())
					.setEstimatedDuration(build.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)));
			EventsService.getExtensionInstance().dispatchEvent(event);

			ListenableFuture<FlowExecution> promise = ((WorkflowRun) r).getExecutionPromise();
			promise.addListener(new Runnable() {
				@Override
				public void run() {
					try {
						FlowExecution ex = ((WorkflowRun) r).getExecutionPromise().get();
						ex.addListener(new WorkflowGraphListener());
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}, executor);
		}
		else{
			if (r.getParent() instanceof MatrixConfiguration) {
				AbstractBuild build = (AbstractBuild) r;
				event = dtoFactory.newDTO(CIEvent.class)
						.setEventType(CIEventType.STARTED)
						.setProject(((MatrixRun) r).getParentBuild().getParent().getName())
						.setProjectDisplayName(((MatrixRun) r).getParentBuild().getParent().getName())
						.setBuildCiId(String.valueOf(build.getNumber()))
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
						.setProjectDisplayName(build.getProject().getName())
						.setBuildCiId(String.valueOf(build.getNumber()))
						.setNumber(String.valueOf(build.getNumber()))
						.setStartTime(build.getStartTimeInMillis())
						.setEstimatedDuration(build.getEstimatedDuration())
						.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)))
						.setParameters(ParameterProcessors.getInstances(build));
				EventsService.getExtensionInstance().dispatchEvent(event);
			}
		}
	}

	@Override
	public void onCompleted(Run r, @Nonnull TaskListener listener) {
		CIBuildResult result;
		if (r.getResult() == Result.SUCCESS) {
			result = CIBuildResult.SUCCESS;
		} else if (r.getResult() == Result.ABORTED) {
			result = CIBuildResult.ABORTED;
		} else if (r.getResult() == Result.FAILURE) {
			result = CIBuildResult.FAILURE;
		} else if (r.getResult() == Result.UNSTABLE) {
			result = CIBuildResult.UNSTABLE;
		} else {
			result = CIBuildResult.UNAVAILABLE;
		}
		if (r instanceof AbstractBuild) {
			Cause.UpstreamCause cause = (Cause.UpstreamCause) r.getCauses().get(0);
			String causeJobName = cause.getUpstreamProject();
			TopLevelItem currentJobParent = Jenkins.getInstance().getItem(causeJobName);
			if(currentJobParent instanceof WorkflowJob)
			{
				return;
			}

			AbstractBuild build = (AbstractBuild) r;
			SCMProcessor scmProcessor = SCMProcessors.getAppropriate(build.getProject().getScm().getClass().getName());
			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.FINISHED)
					.setProject(getProjectName(r))
					.setProjectDisplayName(getProjectName(r))
					.setBuildCiId(String.valueOf(build.getNumber()))
					.setNumber(String.valueOf(build.getNumber()))
					.setStartTime(build.getStartTimeInMillis())
					.setEstimatedDuration(build.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)))
					.setParameters(ParameterProcessors.getInstances(build))
					.setResult(result)
					.setDuration(build.getDuration())
					.setScmData(scmProcessor == null ? null : scmProcessor.getSCMData(build));


			String parentName = currentJobParent.getFullDisplayName()+ String.valueOf(((FreeStyleProject) currentJobParent).getLastBuild().getNumber());
			if (BuildRelations.getInstance().containKey(parentName)) {
				CIEventCause ciEventCause = BuildRelations.getInstance().getValue(parentName);
				List<CIEventCause> ciEventCauseList = new LinkedList<CIEventCause>();
				ciEventCauseList.add(ciEventCause);
				event.setCauses(ciEventCauseList);
			}
			EventsService.getExtensionInstance().dispatchEvent(event);

			GherkinEventsService.copyGherkinTestResultsToBuildDir(build);
			// testListener.processBuild(build);					// need to figure out what it is
		}
		else if (r instanceof WorkflowRun) {
			WorkflowRun build = (WorkflowRun) r;
			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.FINISHED)
					.setProject(getProjectName(r))
					.setBuildCiId(String.valueOf(build.getNumber()))
					.setNumber(String.valueOf(build.getNumber()))
					.setStartTime(build.getStartTimeInMillis())
					.setEstimatedDuration(build.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)))
					.setResult(result)
					.setDuration(build.getDuration());
			EventsService.getExtensionInstance().dispatchEvent(event);
		}
	}

	private String getProjectName(Run r) {
		if (r.getParent() instanceof MatrixConfiguration) {
			return ((MatrixRun) r).getParentBuild().getParent().getName();
		}
		if(r.getParent() instanceof  WorkflowJob)
		{
			return ((WorkflowRun) r).getParent().getName();
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