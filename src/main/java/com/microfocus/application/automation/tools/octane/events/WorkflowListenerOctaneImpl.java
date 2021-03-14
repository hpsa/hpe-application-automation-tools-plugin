/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.events;

import com.google.inject.Inject;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.events.MultiBranchType;
import com.hp.octane.integrations.dto.events.PhaseType;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.model.CIEventCausesFactory;
import com.microfocus.application.automation.tools.octane.model.processors.parameters.ParameterProcessors;
import com.microfocus.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import com.microfocus.application.automation.tools.octane.tests.TestListener;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.Extension;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Octane's listener for WorkflowRun events
 * - this listener should handle Pipeline's STARTED and FINISHED events
 * - this listener should handle each stage's STARTED and FINISHED events
 *
 * User: gadiel
 * Date: 07/06/2016
 * Time: 17:21
 */

@Extension
public class WorkflowListenerOctaneImpl implements GraphListener {
	private static final Logger logger = SDKBasedLoggerProvider.getLogger(WorkflowListenerOctaneImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	//After upgrading Pipeline:Groovy plugin to Version 2.64: receive two start events, therefore
	// pipeline job shows 2 bars for a single pipeline run.
	// Here we add job key during start event and remove key in finished event
	private static Set<String> workflowJobStarted = new HashSet<>();
	@Inject
	private TestListener testListener;

	@Override
	public void onNewHead(FlowNode flowNode) {
		if(!OctaneSDK.hasClients()){
			return;
		}
		try {
			if (BuildHandlerUtils.isWorkflowStartNode(flowNode)) {
				sendPipelineStartedEvent(flowNode);
			} else if (BuildHandlerUtils.isWorkflowEndNode(flowNode)) {
				WorkflowRun parentRun = BuildHandlerUtils.extractParentRun(flowNode);
				sendPipelineFinishedEvent(parentRun);
				BuildLogHelper.enqueueBuildLog(parentRun);
			} else if (BuildHandlerUtils.isStageStartNode(flowNode)) {
				sendStageStartedEvent((StepStartNode) flowNode);
			} else if (BuildHandlerUtils.isStageEndNode(flowNode)) {
				sendStageFinishedEvent((StepEndNode) flowNode);
			}
		} catch (Throwable throwable) {
			logger.error("failed to build and/or dispatch STARTED/FINISHED event for " + flowNode, throwable);
		}
	}

	private void sendPipelineStartedEvent(FlowNode flowNode) {
		WorkflowRun parentRun = BuildHandlerUtils.extractParentRun(flowNode);

		//Avoid duplicate start events
		String buildKey = getBuildKey(parentRun);
		if (workflowJobStarted.contains(buildKey)) {
			return;
		} else {
			workflowJobStarted.add(buildKey);
		}

		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.STARTED)
				.setProjectDisplayName(BuildHandlerUtils.translateFullDisplayName(parentRun.getParent().getFullDisplayName()))
				.setProject(BuildHandlerUtils.getJobCiId(parentRun))
				.setBuildCiId(BuildHandlerUtils.getBuildCiId(parentRun))
				.setNumber(String.valueOf(parentRun.getNumber()))
				.setParameters(ParameterProcessors.getInstances(parentRun))
				.setStartTime(parentRun.getStartTimeInMillis())
				.setEstimatedDuration(parentRun.getEstimatedDuration())
				.setCauses(CIEventCausesFactory.processCauses(parentRun));

		if(isInternal(event.getCauses())){
			event.setPhaseType(PhaseType.INTERNAL);
		}
		if (parentRun.getParent().getParent().getClass().getName().equals(JobProcessorFactory.WORKFLOW_MULTI_BRANCH_JOB_NAME)) {
			event
					.setParentCiId(BuildHandlerUtils.translateFolderJobName(parentRun.getParent().getParent().getFullName()))
					.setMultiBranchType(MultiBranchType.MULTI_BRANCH_CHILD)
					.setProjectDisplayName(BuildHandlerUtils.translateFullDisplayName(parentRun.getParent().getFullDisplayName()));
		}

		CIJenkinsServicesImpl.publishEventToRelevantClients(event);
	}

	private boolean isInternal(List<CIEventCause> causes) {
		if (causes != null) {
			for (CIEventCause cause : causes) {
				if (CIEventCauseType.UPSTREAM.equals(cause.getType())) {
					return true;
				}
			}
		}
		return false;
	}

	private String getBuildKey(WorkflowRun run){
		return run.getFullDisplayName();
	}

	private void sendPipelineFinishedEvent(WorkflowRun parentRun) {
		workflowJobStarted.remove(getBuildKey(parentRun));
		boolean hasTests = testListener.processBuild(parentRun);

		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.FINISHED)
				.setProject(BuildHandlerUtils.getJobCiId(parentRun))
				.setBuildCiId(BuildHandlerUtils.getBuildCiId(parentRun))
				.setNumber(String.valueOf(parentRun.getNumber()))
				.setParameters(ParameterProcessors.getInstances(parentRun))
				.setStartTime(parentRun.getStartTimeInMillis())
				.setEstimatedDuration(parentRun.getEstimatedDuration())
				.setDuration(parentRun.getDuration())
				.setResult(BuildHandlerUtils.translateRunResult(parentRun))
				.setCauses(CIEventCausesFactory.processCauses(parentRun))
				.setTestResultExpected(hasTests);
		CIJenkinsServicesImpl.publishEventToRelevantClients(event);
	}

	private void sendStageStartedEvent(StepStartNode stepStartNode) {
		logger.debug("node " + stepStartNode + " detected as Stage Start node");
		CIEvent event = prepareStageEvent(stepStartNode).setEventType(CIEventType.STARTED);
		CIJenkinsServicesImpl.publishEventToRelevantClients(event);
	}

	private void sendStageFinishedEvent(StepEndNode stepEndNode) {
		logger.debug("node " + stepEndNode + " detected as Stage End node");
		StepStartNode stepStartNode = stepEndNode.getStartNode();
		CIEvent event = prepareStageEvent(stepStartNode)
				.setEventType(CIEventType.FINISHED)
				.setDuration(TimingAction.getStartTime(stepEndNode) - TimingAction.getStartTime(stepStartNode))
				.setResult(extractFlowNodeResult(stepEndNode));

		CIJenkinsServicesImpl.publishEventToRelevantClients(event);
	}

	private CIEvent prepareStageEvent(StepStartNode stepStartNode) {
		WorkflowRun parentRun = BuildHandlerUtils.extractParentRun(stepStartNode);
		return dtoFactory.newDTO(CIEvent.class)
				.setPhaseType(PhaseType.INTERNAL)
				.setIsVirtualProject(true)
				.setProject(stepStartNode.getDisplayName())
				.setBuildCiId(BuildHandlerUtils.getBuildCiId(parentRun))
				.setNumber(String.valueOf(parentRun.getNumber()))
				.setStartTime(TimingAction.getStartTime(stepStartNode))
				.setCauses(CIEventCausesFactory.processCauses(stepStartNode));
	}

	private CIBuildResult extractFlowNodeResult(FlowNode node) {
		return node.getAction(ErrorAction.class) != null ? CIBuildResult.FAILURE : CIBuildResult.SUCCESS;
	}
}