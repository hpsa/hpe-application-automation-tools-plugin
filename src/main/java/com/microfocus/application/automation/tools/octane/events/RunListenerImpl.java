/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.octane.events;

import com.google.inject.Inject;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.events.PhaseType;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.pipelines.PipelinePhase;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.model.CIEventCausesFactory;
import com.microfocus.application.automation.tools.octane.model.processors.parameters.ParameterProcessors;
import com.microfocus.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import com.microfocus.application.automation.tools.octane.model.processors.scm.CommonOriginRevision;
import com.microfocus.application.automation.tools.octane.model.processors.scm.SCMProcessor;
import com.microfocus.application.automation.tools.octane.model.processors.scm.SCMProcessors;
import com.microfocus.application.automation.tools.octane.tests.TestListener;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.scm.SCM;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.List;

/**
 * Run Listener that handles basic CI events and dispatches notifications to the Octane server
 * User: gullery
 * Date: 24/08/14
 * Time: 17:21
 */

@Extension
@SuppressWarnings({"squid:S2259", "squid:S1872", "squid:S1698", "squid:S1132"})
public final class RunListenerImpl extends RunListener<Run> {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Inject
	private TestListener testListener;

	@Override
	public void onStarted(Run run, TaskListener listener) {
		if (noGoConfiguration()) {
			return;
		}
		if (run.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowRun")) {
			return;
		}

		CIEvent event;
		if (run.getParent() instanceof MatrixConfiguration) {
			event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.STARTED)
					.setProject(BuildHandlerUtils.getJobCiId(run))
					.setProjectDisplayName(BuildHandlerUtils.getJobCiId(run))
					.setBuildCiId(BuildHandlerUtils.getBuildCiId(run))
					.setNumber(String.valueOf(run.getNumber()))
					.setStartTime(run.getStartTimeInMillis())
					.setEstimatedDuration(run.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(run))
					.setParameters(ParameterProcessors.getInstances(run));
			if (isInternal(run)) {
				event.setPhaseType(PhaseType.INTERNAL);
			} else {
				event.setPhaseType(PhaseType.POST);
			}
			OctaneSDK.getInstance().getEventsService().publishEvent(event);
		} else if (run instanceof AbstractBuild) {
			event = dtoFactory.newDTO(CIEvent.class)
					.setEventType(CIEventType.STARTED)
					.setProject(BuildHandlerUtils.getJobCiId(run))
					.setProjectDisplayName(BuildHandlerUtils.getJobCiId(run))
					.setBuildCiId(BuildHandlerUtils.getBuildCiId(run))
					.setNumber(String.valueOf(run.getNumber()))
					.setStartTime(run.getStartTimeInMillis())
					.setEstimatedDuration(run.getEstimatedDuration())
					.setCauses(CIEventCausesFactory.processCauses(run))
					.setParameters(ParameterProcessors.getInstances(run));
			if (isInternal(run)) {
				event.setPhaseType(PhaseType.INTERNAL);
			} else {
				event.setPhaseType(PhaseType.POST);
			}
			OctaneSDK.getInstance().getEventsService().publishEvent(event);
		}
	}

	@Override
	public void onFinalized(Run run) {
		if (noGoConfiguration()) {
			return;
		}
		if ("WorkflowRun".equals(run.getClass().getSimpleName())) {
			return;
		}

		CommonOriginRevision commonOriginRevision = getCommonOriginRevision(run);

		boolean hasTests = testListener.processBuild(run);

		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.FINISHED)
				.setBuildCiId(BuildHandlerUtils.getBuildCiId(run))
				.setNumber(String.valueOf(run.getNumber()))
				.setProject(BuildHandlerUtils.getJobCiId(run))
				.setStartTime(run.getStartTimeInMillis())
				.setEstimatedDuration(run.getEstimatedDuration())
				.setCauses(CIEventCausesFactory.processCauses(run))
				.setResult(BuildHandlerUtils.translateRunResult(run))
				.setDuration(run.getDuration())
				.setCommonHashId(commonOriginRevision != null ? commonOriginRevision.revision : null)
				.setBranchName(commonOriginRevision != null ? commonOriginRevision.branch : null)
				.setTestResultExpected(hasTests);

		if (run instanceof AbstractBuild) {
			event.setParameters(ParameterProcessors.getInstances(run))
					.setProjectDisplayName(BuildHandlerUtils.getJobCiId(run));
		}
		OctaneSDK.getInstance().getEventsService().publishEvent(event);
	}

	private boolean noGoConfiguration() {
		return ConfigurationService.getServerConfiguration() == null ||
				!ConfigurationService.getServerConfiguration().isValid() ||
				ConfigurationService.getModel().isSuspend();
	}

	private CommonOriginRevision getCommonOriginRevision(Run r) {
		CommonOriginRevision commonOriginRevision = null;
		if (r instanceof AbstractBuild) {
			final SCM scm = ((AbstractBuild) r).getProject().getScm();
			if (scm != null) {
				SCMProcessor scmProcessor = SCMProcessors.getAppropriate(scm.getClass().getName());
				if (scmProcessor != null) {
					commonOriginRevision = scmProcessor.getCommonOriginRevision(r);
				}
			}
		}
		return commonOriginRevision;
	}

	//  TODO: [YG] this method should be part of causes factory or something like this, it is not suitable for merged build as well
	private boolean isInternal(Run r) {
		boolean result = false;

		//  get upstream cause, if any
		Cause.UpstreamCause upstreamCause = null;
		for (Object cause : r.getCauses()) {
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
				if (parent.getClass().getName().equals(JobProcessorFactory.WORKFLOW_JOB_NAME)) {
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

	private static TopLevelItem getJobFromFolder(String causeJobName) {
		String newJobRefId = causeJobName.substring(0, causeJobName.indexOf('/'));
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
}
