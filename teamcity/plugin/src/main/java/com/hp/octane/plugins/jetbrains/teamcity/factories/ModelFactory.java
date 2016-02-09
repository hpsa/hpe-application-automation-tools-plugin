package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.pipelines.StructurePhase;
import com.hp.nga.integrations.dto.pipelines.StructurePhaseImpl;
import com.hp.nga.integrations.dto.projects.JobsList;
import com.hp.nga.integrations.dto.projects.ProjectConfig;
import com.hp.nga.integrations.dto.projects.ProjectConfigImpl;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.nga.integrations.dto.snapshots.SnapshotPhase;
import com.hp.nga.integrations.dto.snapshots.SnapshotPhaseImpl;
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

    public static JobsList CreateProjectList() {

        JobsList jobsList = DTOFactory.createDTO(JobsList.class);
        List<ProjectConfig> list = new ArrayList<ProjectConfig>();
        List<String>ids = new ArrayList<String>();

        ProjectConfig buildConf;
        for (SProject project : NGAPlugin.getInstance().getProjectManager().getProjects()) {

            List<SBuildType> buildTypes = project.getBuildTypes();
            for (SBuildType buildType : buildTypes) {
                if(!ids.contains(buildType.getInternalId())) {
                    ids.add(buildType.getInternalId());
                    buildConf = new ProjectConfigImpl();
                    buildConf.setName(buildType.getName());
                    buildConf.setCiId(buildType.getExternalId());
                    list.add(buildConf);
                }
            }
        }

        jobsList.setJobs(list.toArray(new ProjectConfig[list.size()]));
        return jobsList;
    }

    public static StructureItem createStructure(String buildConfigurationId) {
        SBuildType root = NGAPlugin.getInstance().getProjectManager().findBuildTypeByExternalId(buildConfigurationId);
        StructureItem treeRoot =null;
        if(root !=null) {
            treeRoot = DTOFactory.createDTO(StructureItem.class);
            treeRoot.setName(root.getName());
            treeRoot.setCiId(root.getExternalId());
            createPipelineStructure(treeRoot, root.getDependencies());

        }else{
            //should update the response?
        }
        return treeRoot;
    }

    private static void createPipelineStructure(StructureItem treeRoot, List<Dependency> dependencies) {
        if(dependencies ==null || dependencies.size() == 0)return;
        StructurePhase phase = new StructurePhaseImpl();
        phase.setName("teamcity_dependencies");
        phase.setBlocking(true);
        List<StructurePhase> structurePhaseList = new ArrayList<StructurePhase>();
        structurePhaseList.add(phase);
        List<StructureItem> structureItemList = new ArrayList<StructureItem>();
        for(Dependency dependency : dependencies){
            SBuildType build = dependency.getDependOn();
            StructureItem buildItem = DTOFactory.createDTO(StructureItem.class);
            buildItem.setName(build.getName());
            buildItem.setCiId(build.getExternalId());
            structureItemList.add(buildItem);
            createPipelineStructure(buildItem, build.getDependencies());
        }
        phase.setJobs(structureItemList);
        treeRoot.setPhasesInternal(structurePhaseList);
    }



    public static SnapshotItem createSnapshot(String buildConfigurationId) {
        SBuildType root = NGAPlugin.getInstance().getProjectManager().findBuildTypeByExternalId(buildConfigurationId);
        //currentBuild.getTriggeredBy().getParameters().get("buildTypeId")
        SnapshotItem snapshotRoot = null;
        if(root !=null) {
            snapshotRoot = createSnapshotItem(root, root.getBuildTypeId());
            createSnapshotPipeline(snapshotRoot, root.getDependencies(), root.getBuildTypeId());

        }else{
            //should update the response?
        }
        return snapshotRoot;
    }

    private static void createSnapshotPipeline(SnapshotItem treeRoot, List<Dependency> dependencies,String rootId) {
        if(dependencies ==null || dependencies.size() == 0)return;
        SnapshotPhase phase = new SnapshotPhaseImpl();
        phase.setBlocking(true);
        phase.setName("teamcity_dependencies");
        List<SnapshotPhase>snapshotPhaseList = new ArrayList<SnapshotPhase>();
        snapshotPhaseList.add(phase);
        List<SnapshotItem> snapshotItemList = new ArrayList<SnapshotItem>();
        for(Dependency dependency : dependencies){
            SBuildType build = dependency.getDependOn();
            SnapshotItem snapshotItem = createSnapshotItem(build,rootId);
            snapshotItemList.add(snapshotItem);
//            phase.setBuilds(snapshotItem);
            createSnapshotPipeline(snapshotItem, build.getDependencies(),rootId);
        }
        phase.setBuilds(snapshotItemList);
        treeRoot.setPhasesInternal(snapshotPhaseList);
    }

    private static SnapshotItem createSnapshotItem(SBuildType build,String rootId){
        //option 1: the build is running now and need to retrieve the data from the running object
        SnapshotItem snapshotItem = createRunningBuild(build, rootId);
        //option 2: the build in the queue
        if(snapshotItem ==null){
            snapshotItem = createQueueBuild(build, rootId);
        }

        if(snapshotItem ==null){
            snapshotItem = createHistoryBuild(build,rootId);
        }
        return snapshotItem;
    }

    private static SnapshotItem createHistoryBuild(SBuildType build, String rootId) {
        SnapshotItem snapshotItem =null;
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
            snapshotItem = DTOFactory.createDTO(SnapshotItem.class);
            snapshotItem.setName(build.getExtendedName());
            snapshotItem.setCiId(build.getExternalId());
            snapshotItem.setDuration(currentBuild.getDuration());
            snapshotItem.setEstimatedDuration(null);
            snapshotItem.setNumber(Integer.parseInt(currentBuild.getBuildNumber()));
            snapshotItem.setStartTime(currentBuild.getClientStartDate().getTime()); //Returns the timestamp when the build was started on the build agent
            snapshotItem.setCauses(null);
            snapshotItem.setStatus(SnapshotStatus.FINISHED);

        }
        return snapshotItem;
    }

    private static SnapshotItem createQueueBuild(SBuildType build, String rootId) {
        SnapshotItem snapshotItem = null;

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
                snapshotItem = DTOFactory.createDTO(SnapshotItem.class);
                snapshotItem.setName(build.getName());
                snapshotItem.setCiId(build.getExternalId());
                snapshotItem.setStatus(SnapshotStatus.QUEUED);
            }
        }
        return snapshotItem;
    }

    private static SnapshotItem createRunningBuild(SBuildType build, String rootId) {

        SnapshotItem snapshotItem =null;
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
            snapshotItem = DTOFactory.createDTO(SnapshotItem.class);
            snapshotItem.setName(build.getName());
            snapshotItem.setCiId(build.getExternalId());
            snapshotItem.setDuration(currentBuild.getDuration());
            snapshotItem.setEstimatedDuration(((SRunningBuild) currentBuild).getDurationEstimate());
            snapshotItem.setNumber(Integer.parseInt(currentBuild.getBuildNumber()));
            snapshotItem.setStartTime(currentBuild.getClientStartDate().getTime()); //Returns the timestamp when the build was started on the build agent
            snapshotItem.setCauses(null);
            snapshotItem.setStatus(SnapshotStatus.RUNNING);
            return snapshotItem;
        }
        return snapshotItem;
    }

}
