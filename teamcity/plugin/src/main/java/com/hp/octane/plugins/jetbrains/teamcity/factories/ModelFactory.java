package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.pipelines.PipelinePhase;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.general.CIJobConfig;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.nga.integrations.dto.snapshots.SnapshotPhase;
import com.hp.nga.integrations.dto.snapshots.SnapshotStatus;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.dependency.Dependency;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 04/01/2016.
 */
public class ModelFactory { // {

    public static CIJobsList CreateProjectList() {

        CIJobsList CIJobsList = DTOFactory.getInstance().newDTO(CIJobsList.class);
        List<CIJobConfig> list = new ArrayList<CIJobConfig>();
        List<String>ids = new ArrayList<String>();

        CIJobConfig buildConf;
        for (SProject project : NGAPlugin.getInstance().getProjectManager().getProjects()) {

            List<SBuildType> buildTypes = project.getBuildTypes();
            for (SBuildType buildType : buildTypes) {
                if(!ids.contains(buildType.getInternalId())) {
                    ids.add(buildType.getInternalId());
                    buildConf = DTOFactory.getInstance().newDTO(CIJobConfig.class);
                    buildConf.setName(buildType.getName());
                    buildConf.setCiId(buildType.getExternalId());
                    list.add(buildConf);
                }
            }
        }

        CIJobsList.setJobs(list.toArray(new CIJobConfig[list.size()]));
        return CIJobsList;
    }

    public static PipelineNode createStructure(String buildConfigurationId) {
        SBuildType root = NGAPlugin.getInstance().getProjectManager().findBuildTypeByExternalId(buildConfigurationId);
        PipelineNode treeRoot =null;
        if(root !=null) {
            treeRoot = DTOFactory.getInstance().newDTO(PipelineNode.class);
            treeRoot.setName(root.getName());
            treeRoot.setCiId(root.getExternalId());
            createPipelineStructure(treeRoot, root.getDependencies());

        }else{
            //should update the response?
        }
        return treeRoot;
    }

    private static void createPipelineStructure(PipelineNode treeRoot, List<Dependency> dependencies) {
        if(dependencies ==null || dependencies.size() == 0)return;
        PipelinePhase phase = DTOFactory.getInstance().newDTO(PipelinePhase.class);
        phase.setName("teamcity_dependencies");
        phase.setBlocking(true);
        List<PipelinePhase> pipelinePhaseList = new ArrayList<PipelinePhase>();
        pipelinePhaseList.add(phase);
        List<PipelineNode> pipelineNodeList = new ArrayList<PipelineNode>();
        for(Dependency dependency : dependencies){
            SBuildType build = dependency.getDependOn();
            PipelineNode buildItem = DTOFactory.getInstance().newDTO(PipelineNode.class);
            buildItem.setName(build.getName());
            buildItem.setCiId(build.getExternalId());
            pipelineNodeList.add(buildItem);
            createPipelineStructure(buildItem, build.getDependencies());
        }
        phase.setJobs(pipelineNodeList);
        treeRoot.setPhasesInternal(pipelinePhaseList);
    }



    public static SnapshotNode createSnapshot(String buildConfigurationId) {
        SBuildType root = NGAPlugin.getInstance().getProjectManager().findBuildTypeByExternalId(buildConfigurationId);
        //currentBuild.getTriggeredBy().getParameters().get("buildTypeId")
        SnapshotNode snapshotRoot = null;
        if(root !=null) {
            snapshotRoot = createSnapshotItem(root, root.getBuildTypeId());
            createSnapshotPipeline(snapshotRoot, root.getDependencies(), root.getBuildTypeId());

        }else{
            //should update the response?
        }
        return snapshotRoot;
    }

    private static void createSnapshotPipeline(SnapshotNode treeRoot, List<Dependency> dependencies,String rootId) {
        if(dependencies ==null || dependencies.size() == 0)return;
        SnapshotPhase phase = DTOFactory.getInstance().newDTO(SnapshotPhase.class);
        phase.setBlocking(true);
        phase.setName("teamcity_dependencies");
        List<SnapshotPhase>snapshotPhaseList = new ArrayList<SnapshotPhase>();
        snapshotPhaseList.add(phase);
        List<SnapshotNode> snapshotNodeList = new ArrayList<SnapshotNode>();
        for(Dependency dependency : dependencies){
            SBuildType build = dependency.getDependOn();
            SnapshotNode snapshotNode = createSnapshotItem(build,rootId);
            snapshotNodeList.add(snapshotNode);
//            phase.setBuilds(snapshotItem);
            createSnapshotPipeline(snapshotNode, build.getDependencies(),rootId);
        }
        phase.setBuilds(snapshotNodeList);
        treeRoot.setPhasesInternal(snapshotPhaseList);
    }

    private static SnapshotNode createSnapshotItem(SBuildType build,String rootId){
        //option 1: the build is running now and need to retrieve the data from the running object
        SnapshotNode snapshotNode = createRunningBuild(build, rootId);
        //option 2: the build in the queue
        if(snapshotNode ==null){
            snapshotNode = createQueueBuild(build, rootId);
        }

        if(snapshotNode ==null){
            snapshotNode = createHistoryBuild(build,rootId);
        }
        return snapshotNode;
    }

    private static SnapshotNode createHistoryBuild(SBuildType build, String rootId) {
        SnapshotNode snapshotNode =null;
        SBuild currentBuild = null;

        List<SFinishedBuild> finishedBuilds = build.getHistory();

        if(build.getBuildTypeId().equalsIgnoreCase(rootId) && finishedBuilds.size()>0){
            currentBuild = finishedBuilds.get(0);
        }else{
            for(SBuild runningBuild : finishedBuilds){
                TriggeredBy trigger = runningBuild.getTriggeredBy();
                if(trigger.getParameters().get("buildTypeId")!=null && rootId.equalsIgnoreCase(trigger.getParameters().get("buildTypeId"))){
                    currentBuild=runningBuild;
                    break;
                }
            }
        }

        if(currentBuild!=null){
            snapshotNode = DTOFactory.getInstance().newDTO(SnapshotNode.class);
            snapshotNode.setName(build.getExtendedName());
            snapshotNode.setCiId(build.getExternalId());
            snapshotNode.setDuration(currentBuild.getDuration());
            snapshotNode.setEstimatedDuration(null);
            snapshotNode.setNumber(Integer.parseInt(currentBuild.getBuildNumber()));
            snapshotNode.setStartTime(currentBuild.getClientStartDate().getTime()); //Returns the timestamp when the build was started on the build agent
            snapshotNode.setCauses(null);
            snapshotNode.setStatus(SnapshotStatus.FINISHED);

        }
        return snapshotNode;
    }

    private static SnapshotNode createQueueBuild(SBuildType build, String rootId) {
        SnapshotNode snapshotNode = null;

        if(build.isInQueue()) {
            List<SQueuedBuild> queuedBuilds = build.getQueuedBuilds(null);
            SQueuedBuild queuedBuild = null;
            if(build.getBuildTypeId().equalsIgnoreCase(rootId) && queuedBuilds.size()>0) {
                queuedBuild = queuedBuilds.get(0);
            }else{
                for (SQueuedBuild runningBuild : queuedBuilds) {
                    TriggeredBy trigger = runningBuild.getTriggeredBy();
                    if (rootId.equalsIgnoreCase(trigger.getParameters().get("buildTypeId"))) {
                        queuedBuild = runningBuild;
                        break;
                    }
                }
            }

            if (queuedBuild != null) {
                snapshotNode = DTOFactory.getInstance().newDTO(SnapshotNode.class);
                snapshotNode.setName(build.getName());
                snapshotNode.setCiId(build.getExternalId());
                snapshotNode.setStatus(SnapshotStatus.QUEUED);
            }
        }
        return snapshotNode;
    }

    private static SnapshotNode createRunningBuild(SBuildType build, String rootId) {

        SnapshotNode snapshotNode =null;
        SBuild currentBuild = null;

        List<SRunningBuild> runningBuilds =build.getRunningBuilds();

        if(build.getBuildTypeId().equalsIgnoreCase(rootId) && runningBuilds.size()>0) {
            currentBuild = runningBuilds.get(0);
        }else{
            for(SBuild runningBuild : runningBuilds){
                TriggeredBy trigger = runningBuild.getTriggeredBy();
                if(rootId.equalsIgnoreCase(trigger.getParameters().get("buildTypeId"))){
                    currentBuild=runningBuild;
                    break;
                }
            }
        }

        if(currentBuild!=null) {
            snapshotNode = DTOFactory.getInstance().newDTO(SnapshotNode.class);
            snapshotNode.setName(build.getName());
            snapshotNode.setCiId(build.getExternalId());
            snapshotNode.setDuration(currentBuild.getDuration());
            snapshotNode.setEstimatedDuration(((SRunningBuild) currentBuild).getDurationEstimate());
            snapshotNode.setNumber(Integer.parseInt(currentBuild.getBuildNumber()));
            snapshotNode.setStartTime(currentBuild.getClientStartDate().getTime()); //Returns the timestamp when the build was started on the build agent
            snapshotNode.setCauses(null);
            snapshotNode.setStatus(SnapshotStatus.RUNNING);
            return snapshotNode;
        }
        return snapshotNode;
    }

}
