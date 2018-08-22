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

package com.microfocus.application.automation.tools.octane.model;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Causes Factory is a collection of static stateless methods to extract/traverse/transform causes chains of the runs
 * User: gullery
 * Date: 20/10/14
 */

public final class CIEventCausesFactory {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	private CIEventCausesFactory() {
	}

	public static List<CIEventCause> processCauses(Run run) {
		if (run == null) {
			throw new IllegalArgumentException("run MUST NOT be null");
		}

		List<CIEventCause> result = new LinkedList<>();
		List<Cause> causes = extractCausesFromRun(run);
		CIEventCause tmpResultCause;
		Cause.UserIdCause tmpUserCause;
		Cause.UpstreamCause tmpUpstreamCause;

		for (Cause cause : causes) {
			tmpResultCause = dtoFactory.newDTO(CIEventCause.class);
			if (cause instanceof SCMTrigger.SCMTriggerCause) {
				tmpResultCause.setType(CIEventCauseType.SCM);
				result.add(tmpResultCause);
			} else if (cause instanceof TimerTrigger.TimerTriggerCause) {
				tmpResultCause.setType(CIEventCauseType.TIMER);
				result.add(tmpResultCause);
			} else if (cause instanceof Cause.UserIdCause) {
				tmpUserCause = (Cause.UserIdCause) cause;
				tmpResultCause.setType(CIEventCauseType.USER);
				tmpResultCause.setUser(tmpUserCause.getUserId());
				result.add(tmpResultCause);
			} else if (cause instanceof Cause.UpstreamCause) {
				tmpUpstreamCause = (Cause.UpstreamCause) cause;

				boolean succeededToBuildFlowCauses = false;
				Run upstreamRun = tmpUpstreamCause.getUpstreamRun();
				if (upstreamRun != null && "WorkflowRun".equals(upstreamRun.getClass().getSimpleName())) {

					//  for the child of the Workflow - break aside and calculate the causes chain of the stages
					WorkflowRun rootWFRun = (WorkflowRun) upstreamRun;
					if (rootWFRun.getExecution() != null && rootWFRun.getExecution().getCurrentHeads() != null) {
						for (FlowNode head : rootWFRun.getExecution().getCurrentHeads()) {
							if (!(head instanceof StepAtomNode)) {
								continue;
							}
							StepAtomNode stepAtomNode = (StepAtomNode) head;
							if (stepAtomNode.getDescriptor() != null &&
									stepAtomNode.getDescriptor().getId().endsWith("BuildTriggerStep") &&
									stepAtomNode.getAction(LabelAction.class) != null) {
								String label = stepAtomNode.getAction(LabelAction.class).getDisplayName();
								if (label != null && label.endsWith(run.getParent().getDisplayName())) {
									List<CIEventCause> flowCauses = processCauses(stepAtomNode);
									result.addAll(flowCauses);
									succeededToBuildFlowCauses = true;
									break;
								}
							}
						}
					}
				}

				if (!succeededToBuildFlowCauses) {

					//  proceed with regular UPSTREAM calculation logic as usual
					tmpResultCause.setType(CIEventCauseType.UPSTREAM);
					tmpResultCause.setProject(resolveJobCiId(tmpUpstreamCause.getUpstreamProject()));
					tmpResultCause.setBuildCiId(String.valueOf(tmpUpstreamCause.getUpstreamBuild()));
					tmpResultCause.setCauses(processCauses(upstreamRun));
					result.add(tmpResultCause);
				}
			} else {
				tmpResultCause.setType(CIEventCauseType.UNDEFINED);
				result.add(tmpResultCause);
			}
		}
		return result;
	}

	public static List<CIEventCause> processCauses(FlowNode flowNode) {
		List<CIEventCause> causes = new LinkedList<>();
		processCauses(flowNode, causes, new LinkedHashSet<>());
		return causes;
	}

	private static void processCauses(FlowNode flowNode, List<CIEventCause> causes, Set<FlowNode> startStagesToSkip) {
		//  we reached the start of the flow - add WorkflowRun as an initial UPSTREAM cause
		if (flowNode.getParents().isEmpty()) {
			WorkflowRun parentRun = BuildHandlerUtils.extractParentRun(flowNode);
			CIEventCause cause = dtoFactory.newDTO(CIEventCause.class)
					.setType(CIEventCauseType.UPSTREAM)
					.setProject(BuildHandlerUtils.getJobCiId(parentRun))
					.setBuildCiId(BuildHandlerUtils.getBuildCiId(parentRun))
					.setCauses(CIEventCausesFactory.processCauses((parentRun)));
			causes.add(cause);
		}

		//  if we are calculating causes for the END STEP - exclude it's own START STEP from calculation
		if (BuildHandlerUtils.isStageEndNode(flowNode)) {
			startStagesToSkip.add(((StepEndNode) flowNode).getStartNode());
		}

		for (FlowNode parent : flowNode.getParents()) {
			if (BuildHandlerUtils.isStageEndNode(parent)) {
				startStagesToSkip.add(((StepEndNode) parent).getStartNode());
				processCauses(parent, causes, startStagesToSkip);
			} else if (BuildHandlerUtils.isStageStartNode(parent)) {
				if (!startStagesToSkip.contains(parent)) {
					CIEventCause cause = dtoFactory.newDTO(CIEventCause.class)
							.setType(CIEventCauseType.UPSTREAM)
							.setProject(parent.getDisplayName())
							.setBuildCiId(String.valueOf(BuildHandlerUtils.extractParentRun(parent).getNumber()));
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

	private static String resolveJobCiId(String jobPlainName) {
		if (!jobPlainName.contains(",")) {
			return BuildHandlerUtils.translateFolderJobName(jobPlainName);
		}
		return jobPlainName;
	}

	private static List<Cause> extractCausesFromRun(Run<?, ?> run) {
		if (run.getParent() instanceof MatrixConfiguration) {
			return ((MatrixRun) run).getParentBuild().getCauses();
		} else {
			return run.getCauses();
		}
	}
}
