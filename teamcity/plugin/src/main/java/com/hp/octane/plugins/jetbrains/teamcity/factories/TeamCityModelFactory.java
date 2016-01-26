package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.nga.integrations.dto.projects.JobsListDTO;
import com.hp.nga.integrations.dto.projects.JobsListDTO.ProjectConfig;
import com.hp.nga.integrations.dto.snapshots.SnapshotStatus;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructurePhase;
import com.hp.octane.plugins.jetbrains.teamcity.model.snapshots.SnapshotItem;
import com.hp.octane.plugins.jetbrains.teamcity.model.snapshots.SnapshotPhase;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.dependency.Dependency;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 04/01/2016.
 */
public class TeamCityModelFactory implements ModelFactory {

    private ProjectManager projectManager;

    public TeamCityModelFactory(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @Override
    public JobsListDTO CreateProjectList() {

        JobsListDTO jobsListDTO = new JobsListDTO();
        List<ProjectConfig> list = new ArrayList<ProjectConfig>();
        List<String>ids = new ArrayList<String>();

        ProjectConfig buildConf;
        for (SProject project :  projectManager.getProjects()) {

            List<SBuildType> buildTypes = project.getBuildTypes();
            for (SBuildType buildType : buildTypes) {
                if(!ids.contains(buildType.getInternalId())) {
                    ids.add(buildType.getInternalId());
                    buildConf = new ProjectConfig();
                    buildConf.setName(buildType.getName());
                    buildConf.setCiId(buildType.getExternalId());
                    list.add(buildConf);
                }
            }
        }

        jobsListDTO.setJobs(list.toArray(new ProjectConfig[list.size()]));
        return jobsListDTO;
    }

    @Override
    public StructureItem createStructure(String buildConfigurationId) {
        SBuildType root = projectManager.findBuildTypeByExternalId(buildConfigurationId);
        StructureItem treeRoot =null;
        if(root !=null) {
            treeRoot = new StructureItem(root.getName(), root.getExternalId());
            createPipelineStructure(treeRoot, root.getDependencies());

        }else{
            //should update the response?
        }
        return treeRoot;
    }

    private void createPipelineStructure(StructureItem treeRoot, List<Dependency> dependencies) {
        if(dependencies ==null || dependencies.size() == 0)return;
        StructurePhase phase = new StructurePhase(true,"teamcity_dependencies");
        for(Dependency dependency : dependencies){
            SBuildType build = dependency.getDependOn();
            StructureItem buildItem = new StructureItem(build.getName(),build.getExternalId());
            phase.addJob(buildItem);
            //treeRoot.addChild(buildItem);
            createPipelineStructure(buildItem, build.getDependencies());
        }
        treeRoot.addPhasesInternal(phase);
    }



    @Override
    public SnapshotItem createSnapshot(String buildConfigurationId) {
        SBuildType root = projectManager.findBuildTypeByExternalId(buildConfigurationId);
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

    private void createSnapshotPipeline(StructureItem treeRoot, List<Dependency> dependencies,String rootId) {
        if(dependencies ==null || dependencies.size() == 0)return;
        SnapshotPhase phase = new SnapshotPhase(true,"teamcity_dependencies");
        for(Dependency dependency : dependencies){
            SBuildType build = dependency.getDependOn();
            SnapshotItem snapshotItem = createSnapshotItem(build,rootId);
            phase.addBuilds(snapshotItem);
            createSnapshotPipeline(snapshotItem, build.getDependencies(),rootId);
        }
        treeRoot.addPhasesInternal(phase);
    }

    private SnapshotItem createSnapshotItem(SBuildType build,String rootId){
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

    private SnapshotItem createHistoryBuild(SBuildType build, String rootId) {
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
            snapshotItem = new SnapshotItem(build.getName(),build.getExternalId());
            snapshotItem.setDuration(currentBuild.getDuration());
            snapshotItem.setEstimatedDuration(null);
            snapshotItem.setNumber(Integer.parseInt(currentBuild.getBuildNumber()));
            snapshotItem.setStartTime(currentBuild.getClientStartDate().getTime()); //Returns the timestamp when the build was started on the build agent
            snapshotItem.setCauses(null);
            snapshotItem.setStatus(SnapshotStatus.FINISHED);

        }
        return snapshotItem;
    }

    private SnapshotItem createQueueBuild(SBuildType build, String rootId) {
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
                snapshotItem = new SnapshotItem(build.getName(),build.getExternalId());

                snapshotItem.setStatus(SnapshotStatus.QUEUED);
            }
        }
        return snapshotItem;
    }

    private SnapshotItem createRunningBuild(SBuildType build, String rootId) {

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
            snapshotItem = new SnapshotItem(build.getName(),build.getExternalId());

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
