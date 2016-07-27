package com.hp.octane.plugins.jenkins.events;

import com.google.inject.Inject;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.plugins.jenkins.model.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import com.hp.octane.plugins.jenkins.tests.TestListener;
import com.hp.octane.plugins.jenkins.tests.gherkin.GherkinEventsService;
import com.hp.octane.plugins.jenkins.model.processors.builders.WorkFlowRunProcessor;
import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
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
		if (r.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowRun")) {
			event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.STARTED)
					.setProject(r.getParent().getName())
					.setBuildCiId(String.valueOf(r.getNumber()))
					.setNumber(String.valueOf(r.getNumber()))
					.setStartTime(r.getStartTimeInMillis())
					.setEstimatedDuration(r.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(r)));
			EventsService.getExtensionInstance().dispatchEvent(event);

			WorkFlowRunProcessor workFlowRunProcessor = new WorkFlowRunProcessor(r);
			workFlowRunProcessor.registerEvents(executor);

		} else {

// turned off at the moment. basically, we want to know if the job called from workflow,
// if so, we want to make sure it did'nt come from a Stage.
// if it did, we don't want to pop this job from here -> we want to pop it from WorkflowGraphListener class.
//			if(calledFromWorkflow(r))
//			{
//				return;
//			}
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

// same as above
//			if(calledFromWorkflow(r))
//			{
//				return;
//			}

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


//			String parentName = currentJobParent.getFullDisplayName()+ String.valueOf(((FreeStyleProject) currentJobParent).getLastBuild().getNumber());
//			if (BuildRelations.getInstance().containKey(parentName)) {
//				CIEventCause ciEventCause = BuildRelations.getInstance().getValue(parentName);
//				List<CIEventCause> ciEventCauseList = new LinkedList<CIEventCause>();
//				ciEventCauseList.add(ciEventCause);
//				event.setCauses(ciEventCauseList);
//			}
			EventsService.getExtensionInstance().dispatchEvent(event);

			GherkinEventsService.copyGherkinTestResultsToBuildDir(build);
			// testListener.processBuild(build);					// need to figure out what it is
		} else if (r.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowRun")) {
			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.FINISHED)
					.setProject(getProjectName(r))
					.setBuildCiId(String.valueOf(r.getNumber()))
					.setNumber(String.valueOf(r.getNumber()))
					.setStartTime(r.getStartTimeInMillis())
					.setEstimatedDuration(r.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(r)))
					.setResult(result)
					.setDuration(r.getDuration());
			EventsService.getExtensionInstance().dispatchEvent(event);
		}
	}

	private String getProjectName(Run r) {
		if (r.getParent() instanceof MatrixConfiguration) {
			return ((MatrixRun) r).getParentBuild().getParent().getName();
		}
		if (r.getParent().getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
			return r.getParent().getName();
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


	private boolean calledFromWorkflow(Run r) {
		try {
			Cause.UpstreamCause cause = (Cause.UpstreamCause) r.getCauses().get(0);
			String causeJobName = cause.getUpstreamProject();
			TopLevelItem currentJobParent = Jenkins.getInstance().getItem(causeJobName);
			if (currentJobParent.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
				return true;
			}
			return false;
		} catch (ClassCastException e) {
			return false;
		}
	}
}