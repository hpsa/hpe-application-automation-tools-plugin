/*
 * © Copyright 2013 EntIT Software LLC
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

import com.cloudbees.workflow.rest.external.StageNodeExt;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;

import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.events.MultiBranchType;
import com.hp.octane.integrations.dto.events.PhaseType;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.microfocus.application.automation.tools.octane.model.CIEventCausesFactory;
import com.microfocus.application.automation.tools.octane.model.processors.parameters.ParameterProcessors;
import com.microfocus.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.Extension;
import hudson.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graph.FlowStartNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;
import java.util.*;

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
public class WorkflowListener implements GraphListener {
	private static final Logger logger = LogManager.getLogger(SCMListenerImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Override
	public void onNewHead(FlowNode flowNode) {
		if (isWorkflowStartNode(flowNode)) {
			sendPipelineStartedEvent(flowNode);
		} else if (isStageStartNode(flowNode)) {
			sendStageStartedEvent((StepStartNode) flowNode);
		} else if (isStageEndNode(flowNode)) {
			sendStageFinishedEvent((StepEndNode) flowNode);
		} else if (isWorkflowEndNode(flowNode)) {
			sendPipelineFinishedEvent((FlowEndNode) flowNode);
		}
	}

	private static boolean isWorkflowStartNode(FlowNode flowNode) {
		return flowNode.getParents().isEmpty() ||
				flowNode.getParents().stream().anyMatch(fn -> fn instanceof FlowStartNode);
	}

	private static boolean isStageStartNode(FlowNode flowNode) {
		return flowNode instanceof StepStartNode && StageNodeExt.isStageNode(flowNode);
	}

	private static boolean isStageEndNode(FlowNode flowNode) {
		return flowNode instanceof StepEndNode && StageNodeExt.isStageNode(((StepEndNode) flowNode).getStartNode());
	}

	private static boolean isWorkflowEndNode(FlowNode flowNode) {
		return flowNode instanceof FlowEndNode;
	}

	private void sendPipelineStartedEvent(FlowNode flowNode) {
		WorkflowRun parentRun = extractParentRun(flowNode);
		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.STARTED)
				.setProject(BuildHandlerUtils.getJobCiId(parentRun))
				.setBuildCiId(BuildHandlerUtils.getBuildCiId(parentRun))
				.setNumber(String.valueOf(parentRun.getNumber()))
				.setParameters(ParameterProcessors.getInstances(parentRun))
				.setStartTime(parentRun.getStartTimeInMillis())
				.setEstimatedDuration(parentRun.getEstimatedDuration())
				.setCauses(extractCausesOfRun(parentRun));

		if (parentRun.getParent().getParent().getClass().getName().equals(JobProcessorFactory.WORKFLOW_MULTI_BRANCH_JOB_NAME)) {
			event
					.setParentCiId(parentRun.getParent().getParent().getFullName())
					.setMultiBranchType(MultiBranchType.MULTI_BRANCH_CHILD)
					.setProjectDisplayName(parentRun.getParent().getFullName());
		}

		OctaneSDK.getInstance().getEventsService().publishEvent(event);
	}

	private void sendStageStartedEvent(StepStartNode stepStartNode) {
		CIEvent event;
		WorkflowRun parentRun = extractParentRun(stepStartNode);
		event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.STARTED)
				.setPhaseType(PhaseType.POST)
				.setProject(stepStartNode.getDisplayName())
				.setBuildCiId(BuildHandlerUtils.getBuildCiId(parentRun))
				.setNumber(String.valueOf(parentRun.getNumber()))
				.setStartTime(TimingAction.getStartTime(stepStartNode))
				.setCauses(extractCausesOfChild(stepStartNode));
		OctaneSDK.getInstance().getEventsService().publishEvent(event);
	}

	private void sendStageFinishedEvent(StepEndNode stepEndNode) {
		WorkflowRun parentRun = extractParentRun(stepEndNode);
		StepStartNode stepStartNode = stepEndNode.getStartNode();
		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.FINISHED)
				.setPhaseType(PhaseType.POST)
				.setProject(stepStartNode.getDisplayName())
				.setBuildCiId(BuildHandlerUtils.getBuildCiId(parentRun))
				.setNumber(String.valueOf(parentRun.getNumber()))
				.setStartTime(TimingAction.getStartTime(stepStartNode))
				.setDuration(TimingAction.getStartTime(stepEndNode) - TimingAction.getStartTime(stepStartNode))
				.setResult(extractFlowNodeResult(stepEndNode))
				.setCauses(extractCausesOfChild(stepEndNode));
		OctaneSDK.getInstance().getEventsService().publishEvent(event);
	}

	private void sendPipelineFinishedEvent(FlowEndNode flowEndNode) {
		WorkflowRun parentRun = extractParentRun(flowEndNode);

		//boolean hasTests = testListener.processBuild(r);

		CIEvent event = dtoFactory.newDTO(CIEvent.class)
				.setEventType(CIEventType.FINISHED)
				.setProject(BuildHandlerUtils.getJobCiId(parentRun))
				.setBuildCiId(BuildHandlerUtils.getBuildCiId(parentRun))
				.setNumber(String.valueOf(parentRun.getNumber()))
				.setParameters(ParameterProcessors.getInstances(parentRun))
				.setStartTime(parentRun.getStartTimeInMillis())
				.setEstimatedDuration(parentRun.getEstimatedDuration())
				.setDuration(parentRun.getDuration())
				.setResult(extractWorkflowResult(parentRun))
				.setCauses(extractCausesOfRun(parentRun))
				.setTestResultExpected(false);
		OctaneSDK.getInstance().getEventsService().publishEvent(event);
	}

	private CIBuildResult extractFlowNodeResult(FlowNode node) {
		for (Action action : node.getActions()) {
			if (action.getClass().equals(ErrorAction.class))
				return CIBuildResult.FAILURE;
		}
		return CIBuildResult.SUCCESS;
	}

	private CIBuildResult extractWorkflowResult(WorkflowRun run) {
		CIBuildResult result;
		if (run.getResult() == Result.SUCCESS) {
			result = CIBuildResult.SUCCESS;
		} else if (run.getResult() == Result.ABORTED) {
			result = CIBuildResult.ABORTED;
		} else if (run.getResult() == Result.FAILURE) {
			result = CIBuildResult.FAILURE;
		} else if (run.getResult() == Result.UNSTABLE) {
			result = CIBuildResult.UNSTABLE;
		} else {
			result = CIBuildResult.UNAVAILABLE;
		}
		return result;
	}

	private List<CIEventCause> extractCausesOfRun(WorkflowRun run) {
		return CIEventCausesFactory.processCauses(run.getCauses());
	}

	private List<CIEventCause> extractCausesOfChild(FlowNode flowNode) {
		List<CIEventCause> causes = new LinkedList<>();
		processCauses(flowNode, causes, new LinkedHashSet<>());
		return causes;
	}

	private static WorkflowRun extractParentRun(FlowNode flowNode) {
		try {
			return (WorkflowRun) flowNode.getExecution().getOwner().getExecutable();
		} catch (IOException ioe) {
			logger.error("failed to extract parent workflow run from " + flowNode, ioe);
			throw new IllegalStateException("failed to extract parent workflow run from " + flowNode);
		}
	}

	private static void processCauses(FlowNode flowNode, List<CIEventCause> causes, Set<FlowNode> startStagesToSkip) {
		//  we reached the start of the flow - add WorkflowRun as an initial UPSTREAM cause
		if (flowNode.getParents().isEmpty()) {
			WorkflowRun parentRun = extractParentRun(flowNode);
			CIEventCause cause = dtoFactory.newDTO(CIEventCause.class)
					.setType(CIEventCauseType.UPSTREAM)
					.setProject(BuildHandlerUtils.getJobCiId(parentRun))
					.setBuildCiId(BuildHandlerUtils.getBuildCiId(parentRun))
					.setCauses(CIEventCausesFactory.processCauses((parentRun.getCauses())));
			causes.add(cause);
		}

		//  if we are calculating causes for the END STEP - exclude it's own START STEP from calculation
		if (isStageEndNode(flowNode)) {
			startStagesToSkip.add(((StepEndNode) flowNode).getStartNode());
		}

		for (FlowNode parent : flowNode.getParents()) {
			if (isStageEndNode(parent)) {
				startStagesToSkip.add(((StepEndNode) parent).getStartNode());
				processCauses(parent, causes, startStagesToSkip);
			} else if (isStageStartNode(parent)) {
				if (!startStagesToSkip.contains(parent)) {
					CIEventCause cause = dtoFactory.newDTO(CIEventCause.class)
							.setType(CIEventCauseType.UPSTREAM)
							.setProject(parent.getDisplayName())
							.setBuildCiId(String.valueOf(extractParentRun(parent).getNumber()));
					causes.add(cause);
					processCauses(parent, cause.getCauses(), startStagesToSkip);
				} else {
					startStagesToSkip.remove(parent);
					processCauses(parent, causes, startStagesToSkip);
				}
			} else {
				processCauses(parent, causes, startStagesToSkip);
			}
		}
	}

	private static FlowNode processCauses(Job job) {
		FlowNode result = null;
		return result;
	}
}