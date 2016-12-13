package com.hp.octane.plugins.jenkins.workflow;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.events.CIEvent;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.Stack;

/**
 * Created by gadiel on 07/06/2016.
 */
public class WorkflowGraphListener implements GraphListener {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private Stack<CIEvent> finishedEventsStack = new Stack<CIEvent>();
//    String lastDisplayName = "";
//    String lastId = "";
//    CIEventCause lastCIEventCause;

	public void onNewHead(FlowNode flowNode) {
//		if (flowNode instanceof StepEndNode) {
//			finishAllStartedEvents();
//		} else if (StageNodeExt.isStageNode(flowNode)) {
//			// its a stage node - it is also a finish of previous stage
//			finishAllStartedEvents();
//			// starting an event:
//			dispatchStartEvent(flowNode);
//			// after the dispatching of the event, we create the finish event and add it to the queue:
//			createFinishEvent(flowNode);
//		} else if (flowNode instanceof StepAtomNode)       // its outer Job
//		{
//			popNewJobEvent(flowNode);       // popping a new event (we want to draw it on NGA)
//		}


	}

//	private void popNewJobEvent(FlowNode flowNode) {
//		// this option is off at the moment.
//		// in general, we want to pop regular jobs that created inside a stage HERE,
//		// and not in RunListenerImpl class.
//		// the reason for that is that we need to care of the CAUSE issue.
//
//
//		// getting real Jenkins object from its name.
////        String buildActualName = flowNode.getDisplayName().replace("Building ", "");
////        TopLevelItem jobAsJenkinsObj = Jenkins.getInstance().getItem(buildActualName);
////
////        if (jobAsJenkinsObj instanceof AbstractProject) {
////            AbstractProject jobAsAbstractProject = (AbstractProject) jobAsJenkinsObj;
////            List<CIEventCause> prevList = new LinkedList<CIEventCause>();
////            prevList.add(lastCIEventCause);
////            CIEventCause cause = new CIEventCauseImpl();
////            cause.setType(CIEventCauseType.UPSTREAM)
////                    .setProject(lastDisplayName)
////                    .setBuildCiId(lastId)
////                    .setCauses(prevList);
////            List<CIEventCause> tempList = new LinkedList<CIEventCause>();
////            tempList.add(cause);
////
////            //creating CIEventCause and add it to the map
////            List<CIEventCause> ListForMap = new LinkedList<CIEventCause>();
////            ListForMap.add(cause);
////            CIEventCause causeForMap = new CIEventCauseImpl();
////            causeForMap.setType(CIEventCauseType.UPSTREAM)
////                    .setProject(jobAsAbstractProject.getDisplayName())
////                    .setBuildCiId(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
////                    .setCauses(ListForMap);
////            BuildRelations.getInstance().addBuildRelation(jobAsAbstractProject.getDisplayName() + String.valueOf(jobAsAbstractProject.getLastBuild().getNumber() + 1), causeForMap);
////
////
////            // start event
////            CIEvent event = dtoFactory.newDTO(CIEvent.class)
////                    .setEventType(CIEventType.STARTED)
////                    .setProject(jobAsAbstractProject.getDisplayName())
////                    .setBuildCiId(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
////                    .setNumber(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
////                    .setStartTime(jobAsAbstractProject.getLastBuild().getStartTimeInMillis())
////                    .setEstimatedDuration(jobAsAbstractProject.getLastBuild().getEstimatedDuration())
////                    .setCauses(tempList);
////            EventsService.getExtensionInstance().dispatchEvent(event);
////
////            // finishEvent
////            CIEventCause endCause = new CIEventCauseImpl();
////            endCause.setType(CIEventCauseType.UPSTREAM)
////                    .setProject(lastDisplayName)
////                    .setBuildCiId(lastId)
////                    .setCauses(prevList);
////            List<CIEventCause> endTempList = new LinkedList<CIEventCause>();
////            endTempList.add(endCause);
////            CIEvent endEvent = dtoFactory.newDTO(CIEvent.class)
////                    .setEventType(CIEventType.FINISHED)
////                    .setProject(jobAsAbstractProject.getDisplayName())
////                    .setBuildCiId(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
////                    .setNumber(String.valueOf(jobAsAbstractProject.getLastBuild().getNumber()))
////                    .setStartTime(12345678910L)
////                    .setEstimatedDuration(12345678910L)
////                    .setCauses(endTempList)
////                    .setResult(CIBuildResult.SUCCESS)
////                    .setDuration(12345678910L);
////            finishedEventsQueue.add(endEvent);
////        }
//
//	}
//
//
//	private void finishAllStartedEvents() {
//		while (!finishedEventsStack.isEmpty()) {
//			EventsService.getExtensionInstance().dispatchEvent(finishedEventsStack.pop());
//		}
//	}
//
//	private List<? extends Cause> extractCauses(Run r) {
//		return r.getCauses();
//	}
//
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
//
//
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
//
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
//
//	private List<CIEventCause> getCIEventCause(FlowNode flowNode) {
//		String ParentName = getParentName(flowNode);
//		String ParentBuildNum = getParentId(flowNode);
//		CIEventCause cause = dtoFactory.newDTO(CIEventCause.class)
//				.setType(CIEventCauseType.UPSTREAM)
//				.setProject(ParentName)
//				.setBuildCiId(ParentBuildNum);
//		List<CIEventCause> tempList = new LinkedList<CIEventCause>();
//		tempList.add(cause);
//		return tempList;
//
//	}
}