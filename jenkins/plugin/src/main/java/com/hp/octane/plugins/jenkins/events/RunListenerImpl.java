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
import com.hp.octane.plugins.jenkins.tests.gherkin.GherkinEventsService;
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


    private boolean isInternal(Run r) {

        try {
            Cause.UpstreamCause cause = (Cause.UpstreamCause) r.getCauses().get(0);
            String causeJobName = cause.getUpstreamProject();
            TopLevelItem currentJobParent = Jenkins.getInstance().getItem(causeJobName);
            if (currentJobParent == null) {
                if (causeJobName.contains("/") && !causeJobName.contains(",")) {
                    currentJobParent = getJobFromFolder(causeJobName);
                    if(currentJobParent==null){
                        return false;
                    }
                }
            }
            // for Pipeline As A Code Plugin
            if (currentJobParent.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
                return true;
            }
            // for MultiJob or Matrix Plugin
            if (currentJobParent.getClass().getName().equals("com.tikal.jenkins.plugins.multijob.MultiJobProject") ||
                    currentJobParent.getClass().getName().equals("hudson.matrix.MatrixProject")) {
                List<PipelinePhase> phases = JobProcessorFactory.getFlowProcessor(((AbstractProject) currentJobParent)).getInternals();
                for (PipelinePhase p : phases) {
                    for (PipelineNode n : p.getJobs()) {
                        if (n.getName().equals(r.getParent().getName())) {
                            return true;
                        }
                    }
                }
                return false;
            }
            return false;     // Elsewhere
        } catch (ClassCastException e) {
            return false;   // happens only in the root node
        }
    }

    TopLevelItem getJobFromFolder(String causeJobName) {
        String newJobRefId = causeJobName.substring(0, causeJobName.indexOf("/"));
        TopLevelItem item = Jenkins.getInstance().getItem(newJobRefId);
        if (item != null) {
            Collection<? extends Job> allJobs = item.getAllJobs();
            String cleanJobName = causeJobName.substring(causeJobName.lastIndexOf("/") + 1);
            for (Job job : allJobs) {
                if (causeJobName.endsWith(job.getName())) {
                    return (TopLevelItem) job;
                }
            }
            return null;
        }
        return null;
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
            GherkinEventsService.copyGherkinTestResultsToBuildDir(build);
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