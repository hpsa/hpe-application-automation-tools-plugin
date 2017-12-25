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
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.octane.workflow;


import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;

import hudson.Extension;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;
import java.util.*;

/**
 * Created by gadiel on 07/06/2016.
 */
@Extension
public class WorkflowGraphListener implements GraphListener {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
//	private Stack<CIEvent> finishedEventsStack = new Stack<CIEvent>();
//    String lastDisplayName = "";
//    String lastId = "";
//    CIEventCause lastCIEventCause;
	public static FlowNodeContainer container;
	public void onNewHead(FlowNode flowNode) {
		if (flowNode instanceof StepEndNode) {
			//finishAllStartedEvents();
			WorkspaceAction workspaceAction =((StepEndNode) flowNode).getStartNode().getAction(WorkspaceAction.class);
			if(workspaceAction!=null){

				try {
					WorkflowRun run = (WorkflowRun) flowNode.getExecution().getOwner().getExecutable();
					//TaskListener listener = flowNode.getExecution().getOwner().getListener();
					WorkflowBuildAdapter adapter = new WorkflowBuildAdapter(run.getParent(), run, workspaceAction.getWorkspace());
					FlowNodeContainer.addFlowNode(adapter);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
//		} else if (StageNodeExt.isStageNode(flowNode)) {
//			// its a stage node - it is also a finish of previous stage
//			finishAllStartedEvents();
//			// starting an event:
//			dispatchStartEvent(flowNode);
//			// after the dispatching of the event, we create the finish event and add it to the queue:
//			createFinishEvent(flowNode);
		}
//		else if (flowNode instanceof StepAtomNode)       // its outer Job
//		{
//			if(isItStageStep(flowNode)){
//				// its a stage node - it is also a finish of previous stage
//				finishAllStartedEvents();
//				// starting an event:
//				dispatchStartEvent(flowNode);
//				// after the dispatching of the event, we create the finish event and add it to the queue:
//				createFinishEvent(flowNode);
//			}else if( isItBuildStep(flowNode))
//				popNewJobEvent(flowNode);       // popping a new event (we want to draw it on NGA)
//		}

	}

//	private boolean isItBuildStep(FlowNode flowNode) {
//		return ((StepAtomNode) flowNode).getDescriptor().getFunctionName().equalsIgnoreCase("build");
//	}
//
//	private boolean isItStageStep(FlowNode flowNode) {
//		return ((StepAtomNode) flowNode).getDescriptor().getFunctionName().equalsIgnoreCase("stage");
//	}

//	private void popNewJobEvent(FlowNode flowNode) {
//		// this option is off at the moment.
//		// in general, we want to pop regular jobs that created inside a stage HERE,
//		// and not in RunListenerImpl class.
//		// the reason for that is that we need to care of the CAUSE issue.
//
//
//		// getting real Jenkins object from its name.
//        String buildActualName = flowNode.getDisplayName().replace("Building ", "");
//        TopLevelItem jobAsJenkinsObj = Jenkins.getInstance().getItem(buildActualName);
//
//        if (jobAsJenkinsObj instanceof AbstractProject) {
//            AbstractProject jobAsAbstractProject = (AbstractProject) jobAsJenkinsObj;
//            List<CIEventCause> prevList = new LinkedList<CIEventCause>();
//            prevList.add(lastCIEventCause);
//            CIEventCause cause = dtoFactory.newDTO(CIEventCause.class);
//            cause.setType(CIEventCauseType.UPSTREAM)
//                    .setProject(lastDisplayName)
//                    .setBuildCiId(lastId)
//                    .setCauses(prevList);
//            List<CIEventCause> tempList = new LinkedList<CIEventCause>();
//            tempList.add(cause);
//
//            //creating CIEventCause and add it to the map
//            List<CIEventCause> ListForMap = new LinkedList<CIEventCause>();
//            ListForMap.add(cause);
//            CIEventCause causeForMap = dtoFactory.newDTO(CIEventCause.class);
//            causeForMap.setType(CIEventCauseType.UPSTREAM)
//                    .setProject(jobAsAbstractProject.getDisplayName())
//                    .setBuildCiId(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
//                    .setCauses(ListForMap);
//            BuildRelations.getInstance().addBuildRelation(jobAsAbstractProject.getDisplayName() + String.valueOf(jobAsAbstractProject.getLastBuild().getNumber() + 1), causeForMap);
//
//
//            // start event
//            CIEvent event = dtoFactory.newDTO(CIEvent.class)
//                    .setEventType(CIEventType.STARTED)
//                    .setProject(jobAsAbstractProject.getDisplayName())
//                    .setBuildCiId(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
//                    .setNumber(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
//                    .setStartTime(jobAsAbstractProject.getLastBuild().getStartTimeInMillis())
//                    .setEstimatedDuration(jobAsAbstractProject.getLastBuild().getEstimatedDuration())
//                    .setCauses(tempList);
//            EventsService.getExtensionInstance().dispatchEvent(event);
//
//            // finishEvent
//            CIEventCause endCause = dtoFactory.newDTO(CIEventCause.class);
//            endCause.setType(CIEventCauseType.UPSTREAM)
//                    .setProject(lastDisplayName)
//                    .setBuildCiId(lastId)
//                    .setCauses(prevList);
//            List<CIEventCause> endTempList = new LinkedList<CIEventCause>();
//            endTempList.add(endCause);
//            CIEvent endEvent = dtoFactory.newDTO(CIEvent.class)
//                    .setEventType(CIEventType.FINISHED)
//                    .setProject(jobAsAbstractProject.getDisplayName())
//                    .setBuildCiId(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
//                    .setNumber(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
//                    .setStartTime(12345678910L)
//                    .setEstimatedDuration(12345678910L)
//                    .setCauses(endTempList)
//                    .setResult(CIBuildResult.SUCCESS)
//                    .setDuration(12345678910L);
////            finishedEventsQueue.add(endEvent);
//        }
//
//	}


//	private void finishAllStartedEvents() {
//		while (!finishedEventsStack.isEmpty()) {
//			EventsService.getExtensionInstance().dispatchEvent(finishedEventsStack.pop());
//		}
//	}

//	private List<? extends Cause> extractCauses(Run r) {
//		return r.getCauses();
//	}

//	private String getParentName(FlowNode flowNode) {
//
//		String url = null;
//		try {
//			url = flowNode.getUrl();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		String[] words = url.split("/");
//		return words[1];
//	}


//	private String getParentId(FlowNode flowNode) {
//		String url = null;
//		try {
//			url = flowNode.getUrl();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		String[] words = url.split("/");
//		return words[2];
//	}

//
//	private void dispatchStartEvent(FlowNode flowNode) {
//		Long nodeStartTime = 0L;
//		if ((flowNode.getAllActions().get(3) instanceof TimingAction)) {
//			TimingAction t = (TimingAction) flowNode.getAllActions().get(3);
//			nodeStartTime = t.getStartTime();
//		}
//		List<CIEventCause> causeList = getCIEventCause(flowNode);
//		CIEvent event = dtoFactory.newDTO(CIEvent.class)
//				.setEventType(CIEventType.STARTED)
//				.setProject(flowNode.getDisplayName())
//				.setBuildCiId(String.valueOf(flowNode.getId()))
//				.setNumber(flowNode.getId())
//				.setStartTime(nodeStartTime)
//				.setEstimatedDuration(FlowNodeUtil.getStageExecDuration(flowNode).getPauseDurationMillis())
//				.setCauses(causeList);
//		EventsService.getExtensionInstance().dispatchEvent(event);
////        lastDisplayName = flowNode.getDisplayName();
////        lastId = String.valueOf(flowNode.getId());
////        lastCIEventCause = causeList.get(0);
//	}
////
//	private void createFinishEvent(FlowNode flowNode) {
//		String ParentName = getParentName(flowNode);
//		String ParentBuildNum = getParentId(flowNode);
//		CIEventCause endCause = dtoFactory.newDTO(CIEventCause.class)
//				.setType(CIEventCauseType.UPSTREAM)
//				.setProject(ParentName)
//				.setBuildCiId(ParentBuildNum);
//		List<CIEventCause> endTempList = new LinkedList<CIEventCause>();
//		endTempList.add(endCause);
//		CIEvent endEvent = dtoFactory.newDTO(CIEvent.class)
//				.setEventType(CIEventType.FINISHED)
//				.setProject(flowNode.getDisplayName())
//				.setBuildCiId(String.valueOf(flowNode.getId()))
//				.setNumber(flowNode.getId())
//				.setStartTime(12345678910L)
//				.setEstimatedDuration(12345678910L)
//				.setCauses(endTempList)
//				.setResult(CIBuildResult.SUCCESS)
//				.setDuration(12345678910L);
//		finishedEventsStack.push(endEvent);
//	}

//	private List<CIEventCause> getCIEventCause(FlowNode flowNode) {
//		String ParentName = getParentName(flowNode);
//		String ParentBuildNum = getParentId(flowNode);
//		CIEventCause cause = dtoFactory.newDTO(CIEventCause.class)
//				.setType(CIEventCauseType.UPSTREAM)
//				.setProject(ParentName)
//				.setBuildCiId(ParentBuildNum);
//		List<CIEventCause> tempList = new LinkedList<>();
//		tempList.add(cause);
//		return tempList;
//
//	}

	public static class FlowNodeContainer{
		private static Map<String, List<Run>> map = new HashMap<>();

		protected static void addFlowNode(Run run){
			String key = getKey(run);
			List<Run> list = map.get(key);

			if(list == null){
				list = new ArrayList<>();
				map.put(key,list);
			}
			list.add(run);
		}

		public static List<Run> getFlowNode(Run run){
			return map.remove(getKey(run));
		}

		private static String getKey(Run run) {
			return run.getId() + run.getParent().getName();
		}
	}
}