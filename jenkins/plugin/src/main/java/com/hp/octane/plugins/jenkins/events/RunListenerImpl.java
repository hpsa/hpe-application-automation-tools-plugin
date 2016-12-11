package com.hp.octane.plugins.jenkins.events;

import com.google.inject.Inject;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.events.PhaseType;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.pipelines.PipelinePhase;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.plugins.jenkins.model.CIEventCausesFactory;
import com.hp.octane.plugins.jenkins.model.processors.builders.WorkFlowRunProcessor;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.projects.JobProcessorFactory;
import com.hp.octane.plugins.jenkins.tests.TestListener;
import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/08/14
 * Time: 17:21
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
					.setProject(JobProcessorFactory.getFlowProcessor(r.getParent()).getJobCiId())
					.setBuildCiId(String.valueOf(r.getNumber()))
					.setNumber(String.valueOf(r.getNumber()))
					.setStartTime(r.getStartTimeInMillis())
					.setPhaseType(PhaseType.POST)
					.setEstimatedDuration(r.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(r)));
			EventsService.getExtensionInstance().dispatchEvent(event);
			WorkFlowRunProcessor workFlowRunProcessor = new WorkFlowRunProcessor(r);
			workFlowRunProcessor.registerEvents(executor);
		} else {
			if (r.getParent() instanceof MatrixConfiguration) {
				AbstractBuild build = (AbstractBuild) r;
				event = dtoFactory.newDTO(CIEvent.class)
						.setEventType(CIEventType.STARTED)
						.setProject(getJobCiId(r))
						.setProjectDisplayName(getJobCiId(r))
						.setBuildCiId(String.valueOf(build.getNumber()))
						.setNumber(String.valueOf(build.getNumber()))
						.setStartTime(build.getStartTimeInMillis())
						.setEstimatedDuration(build.getEstimatedDuration())
						.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)))
						.setParameters(ParameterProcessors.getInstances(build));
				if (isInternal(r)) {
					event.setPhaseType(PhaseType.INTERNAL);
				} else {
					event.setPhaseType(PhaseType.POST);
				}
				EventsService.getExtensionInstance().dispatchEvent(event);
			} else if (r instanceof AbstractBuild) {
				AbstractBuild build = (AbstractBuild) r;
				event = dtoFactory.newDTO(CIEvent.class)
						.setEventType(CIEventType.STARTED)
						.setProject(getJobCiId(r))
						.setProjectDisplayName(getJobCiId(r))
						.setBuildCiId(String.valueOf(build.getNumber()))
						.setNumber(String.valueOf(build.getNumber()))
						.setStartTime(build.getStartTimeInMillis())
						.setEstimatedDuration(build.getEstimatedDuration())
						.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)))
						.setParameters(ParameterProcessors.getInstances(build));
				if (isInternal(r)) {
					event.setPhaseType(PhaseType.INTERNAL);
				} else {
					event.setPhaseType(PhaseType.POST);
				}
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
			AbstractBuild build = (AbstractBuild) r;
			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.FINISHED)
					.setProject(getJobCiId(r))
					.setProjectDisplayName(getJobCiId(r))
					.setBuildCiId(String.valueOf(build.getNumber()))
					.setNumber(String.valueOf(build.getNumber()))
					.setStartTime(build.getStartTimeInMillis())
					.setEstimatedDuration(build.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(extractCauses(build)))
					.setParameters(ParameterProcessors.getInstances(build))
					.setResult(result)
					.setDuration(build.getDuration());
			EventsService.getExtensionInstance().dispatchEvent(event);
			testListener.processBuild(build, listener);
		} else if (r.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowRun")) {
			CIEvent event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.FINISHED)
					.setProject(getJobCiId(r))
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

	//  TODO: [YG] this method should be part of causes factory or something like this, it is not suitable for merged build as well
	private boolean isInternal(Run r) {
		boolean result = false;

		//  get upstream cause, if any
		Cause.UpstreamCause upstreamCause = null;
		for (Cause cause : (List<Cause>) r.getCauses()) {
			if (cause instanceof Cause.UpstreamCause) {
				upstreamCause = (Cause.UpstreamCause) cause;
				break;          //  TODO: here we are breaking the merged build support
			}
		}

		if (upstreamCause != null) {
			String causeJobName = upstreamCause.getUpstreamProject();
			TopLevelItem parent = Jenkins.getInstance().getItem(causeJobName);
			if (parent == null) {
				if (causeJobName.contains("/") && !causeJobName.contains(",")) {
					parent = getJobFromFolder(causeJobName);
					if (parent == null) {
						result = false;
					}
				}
			} else {
				if (parent.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
					result = true;
				} else {
					List<PipelinePhase> phases = JobProcessorFactory.getFlowProcessor((Job) parent).getInternals();
					for (PipelinePhase p : phases) {
						for (PipelineNode n : p.getJobs()) {
							if (n != null && n.getName().equals(r.getParent().getName())) {
								return true;
							}
						}
					}
					return false;
				}
			}
		}

		return result;
	}

	private TopLevelItem getJobFromFolder(String causeJobName) {
		String newJobRefId = causeJobName.substring(0, causeJobName.indexOf("/"));
		TopLevelItem item = Jenkins.getInstance().getItem(newJobRefId);
		if (item != null) {
			Collection<? extends Job> allJobs = item.getAllJobs();
			for (Job job : allJobs) {
				if (causeJobName.endsWith(job.getName())) {
					return (TopLevelItem) job;
				}
			}
			return null;
		}
		return null;
	}

	private String getJobCiId(Run r) {
		if (r.getParent() instanceof MatrixConfiguration) {
			return JobProcessorFactory.getFlowProcessor(((MatrixRun) r).getParentBuild().getParent()).getJobCiId();
		}
		if (r.getParent().getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
			return r.getParent().getName();
		}
		return JobProcessorFactory.getFlowProcessor(((AbstractBuild) r).getProject()).getJobCiId();
	}

	private List<Cause> extractCauses(Run r) {
		if (r.getParent() instanceof MatrixConfiguration) {
			return ((MatrixRun) r).getParentBuild().getCauses();
		} else {
			return r.getCauses();
		}
	}
}