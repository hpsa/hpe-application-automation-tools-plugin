package com.hp.octane.plugins.jenkins.workflow;

import com.cloudbees.workflow.flownode.FlowNodeUtil;
import com.cloudbees.workflow.rest.external.StageNodeExt;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.plugins.jenkins.events.EventsService;
import hudson.model.Cause;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
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
        if (flowNode instanceof StepEndNode) {
            finishAllStartedEvents();
        } else if (StageNodeExt.isStageNode(flowNode)) {
            // its a stage node - also a finish of previous stage
            finishAllStartedEvents();
            // starting an event:
            dispatchStartEvent(flowNode);
            // after the dispatching of the event, we create the finish event and add it to the queue:
             dispatchFinishEvent(flowNode);
        }
        else if (flowNode instanceof StepAtomNode)       // its outer Job
        {
            popNewJobEvent(flowNode);       // popping a new event (we want to draw it on NGA)
        }


    }

    private void popNewJobEvent(FlowNode flowNode) {
        // getting real Jenkins object from its name.
//        String buildActualName = flowNode.getDisplayName().replace("Building ", "");
//        TopLevelItem jobAsJenkinsObj = Jenkins.getInstance().getItem(buildActualName);
//
//        if (jobAsJenkinsObj instanceof AbstractProject) {
//            AbstractProject jobAsAbstractProject = (AbstractProject) jobAsJenkinsObj;
//            List<CIEventCause> prevList = new LinkedList<CIEventCause>();
//            prevList.add(lastCIEventCause);
//            CIEventCause cause = new CIEventCauseImpl();
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
//            CIEventCause causeForMap = new CIEventCauseImpl();
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
//            CIEventCause endCause = new CIEventCauseImpl();
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
//            finishedEventsQueue.add(endEvent);
//        }

    }


    private void finishAllStartedEvents() {
        while (!finishedEventsStack.isEmpty()) {
            EventsService.getExtensionInstance().dispatchEvent(finishedEventsStack.pop());
        }
    }

    private List<? extends Cause> extractCauses(Run r) {
        return r.getCauses();
    }

    private String getParentName(FlowNode flowNode) {

        String url = null;
        try {
            url = flowNode.getUrl();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int count = 0;
        int firstIndex = 0;
        int secondIndex = 0;
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) == '/') {
                count++;
                if (count == 1) {
                    firstIndex = i;
                } else if (count == 2) {
                    secondIndex = i;
                    break;
                }
            }
        }
        String parentName = url.substring(firstIndex + 1, secondIndex);
        return parentName;
    }


    private String getParentId(FlowNode flowNode) {
        String url = null;
        try {
            url = flowNode.getUrl();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int count = 0;
        int secondIndex = 0;
        int thirdIndex = 0;
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) == '/') {
                count++;
                if (count == 2) {
                    secondIndex = i;
                } else if (count == 3) {
                    thirdIndex = i;
                    break;
                }
            }
        }
        String parentBuildNum = url.substring(secondIndex + 1, thirdIndex);
        return parentBuildNum;
    }


    private void dispatchStartEvent(FlowNode flowNode) {

        Long nodeStartTime = 0L;
        if ((flowNode.getAllActions().get(3) instanceof TimingAction)) {
            TimingAction t = (TimingAction) flowNode.getAllActions().get(3);
            nodeStartTime = t.getStartTime();
        }
        List<CIEventCause> causeList =  getCIEventCause(flowNode);
        CIEvent event = dtoFactory.newDTO(CIEvent.class)
                .setEventType(CIEventType.STARTED)
                .setProject(flowNode.getDisplayName())
                .setBuildCiId(String.valueOf(flowNode.getId()))
                .setNumber(flowNode.getId())
                .setStartTime(nodeStartTime)
                .setEstimatedDuration(FlowNodeUtil.getStageExecDuration(flowNode).getPauseDurationMillis())
                .setCauses(causeList);
        EventsService.getExtensionInstance().dispatchEvent(event);
//        lastDisplayName = flowNode.getDisplayName();
//        lastId = String.valueOf(flowNode.getId());
//        lastCIEventCause = causeList.get(0);
    }

    private void dispatchFinishEvent(FlowNode flowNode) {
        String ParentName = getParentName(flowNode);
        String ParentBuildNum = getParentId(flowNode);
        CIEventCause endCause = new CIEventCauseImpl();
        endCause.setType(CIEventCauseType.UPSTREAM)
                .setProject(ParentName)
                .setBuildCiId(ParentBuildNum);
        List<CIEventCause> endTempList = new LinkedList<CIEventCause>();
        endTempList.add(endCause);
        CIEvent endEvent = dtoFactory.newDTO(CIEvent.class)
                .setEventType(CIEventType.FINISHED)
                .setProject(flowNode.getDisplayName())
                .setBuildCiId(String.valueOf(flowNode.getId()))
                .setNumber(flowNode.getId())
                .setStartTime(12345678910L)
                .setEstimatedDuration(12345678910L)
                .setCauses(endTempList)
                .setResult(CIBuildResult.SUCCESS)
                .setDuration(12345678910L);
        finishedEventsStack.push(endEvent);
    }


    private List<CIEventCause> getCIEventCause(FlowNode flowNode)
    {
        String ParentName = getParentName(flowNode);
        String ParentBuildNum = getParentId(flowNode);
        CIEventCause cause = new CIEventCauseImpl();
        cause.setType(CIEventCauseType.UPSTREAM)
                .setProject(ParentName)
                .setBuildCiId(ParentBuildNum);
        List<CIEventCause> tempList = new LinkedList<CIEventCause>();
        tempList.add(cause);
        return tempList;

    }


}