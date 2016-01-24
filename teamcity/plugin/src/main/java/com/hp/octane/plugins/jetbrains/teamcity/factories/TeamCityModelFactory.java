package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.nga.integrations.dto.projects.ProjectsList;
import com.hp.nga.integrations.dto.projects.ProjectsList.ProjectConfig;
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
    public ProjectsList CreateProjectList() {

        ProjectsList projectsList = new ProjectsList();
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

        projectsList.setJobs(list.toArray(new ProjectConfig[list.size()]));
        return projectsList;
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
        SnapshotItem snapshotRoot = null;
        if(root !=null) {
            snapshotRoot = createSnapshotItem(root);
            createSnapshotPipeline(snapshotRoot, root.getDependencies(), root);

        }else{
            //should update the response?
        }
        return snapshotRoot;
    }

    private void createSnapshotPipeline(StructureItem treeRoot, List<Dependency> dependencies,SBuildType root) {
        if(dependencies ==null || dependencies.size() == 0)return;
        SnapshotPhase phase = new SnapshotPhase(true,"teamcity_dependencies");
        for(Dependency dependency : dependencies){
            SBuildType build = dependency.getDependOn();
            SnapshotItem snapshotItem = createSnapshotItem(build);
            phase.addBuilds(snapshotItem);
            createSnapshotPipeline(snapshotItem, build.getDependencies(),root);
        }
        treeRoot.addPhasesInternal(phase);
    }

    private  SnapshotItem createSnapshotItem(SBuildType build){
        SnapshotItem snapshotItem = new SnapshotItem(build.getName(),build.getExternalId());

        //option 1: the build is running now and need to retrieve the data from the running object
        List<SRunningBuild> runningBuilds =build.getRunningBuilds();
        SRunningBuild currentBuild = null;
        for(SRunningBuild runningBuild : runningBuilds){
            TriggeredBy trigger = runningBuild.getTriggeredBy();
            currentBuild=runningBuild;
        }
        if(currentBuild!=null) {
            snapshotItem.setDuration(currentBuild.getDuration());
            snapshotItem.setEstimatedDuration(currentBuild.getDurationEstimate());
            snapshotItem.setNumber(Integer.parseInt(currentBuild.getBuildNumber()));
            snapshotItem.setStatus(currentBuild.getBuildStatus().getText());
            snapshotItem.setStartTime(currentBuild.getClientStartDate().getTime()); //Returns the timestamp when the build was started on the build agent

        }

        //option 2: the build not running now and need to get the data from the history object

        return snapshotItem;
    }
//    @Override
//    public SnapshotItem createSnapshot(String buildConfigurationId, String buildNumber) {
//        SBuildType root = projectManager.findBuildTypeByExternalId(buildConfigurationId);
//
//        //option 1: the build is running now and need to retrieve the data from the running object
//        List<SRunningBuild> runningBuilds =root.getRunningBuilds();
//        SRunningBuild currentBuild = null;
//        for(SRunningBuild runningBuild : runningBuilds){
//            String currentPath = runningBuild.getCurrentPath();
//            TriggeredBy trigger = runningBuild.getTriggeredBy();
//            String status = runningBuild.getBuildStatus().getText();
//            currentBuild=runningBuild;
//            //trigger.getAsString()
//        }
//        SnapshotItem snapshotRoot = new SnapshotItem(root.getName(),root.getExternalId());
//        if(currentBuild!=null) {
////            snapshotRoot.setName(root.getName());
////            snapshotRoot.setId(root.getExternalId());
//            snapshotRoot.setDuration(currentBuild.getDuration());
//            snapshotRoot.setEstimatedDuration(currentBuild.getDurationEstimate());
//            snapshotRoot.setNumber(Integer.parseInt(currentBuild.getBuildNumber()));
//        }
//
//        //option 2: the build not running now and need to get the data from the history object
//        return snapshotRoot;
//    }

}
