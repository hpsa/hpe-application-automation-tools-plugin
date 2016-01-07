package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectConfig;
import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectsList;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.TreeItem;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.TreeItemContainer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

import java.util.ArrayList;
import java.util.Collection;
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

        List<ProjectConfig> list = new ArrayList<ProjectConfig>();
        List<String>ids = new ArrayList<String>();

        ProjectConfig buildConf;
        for (SProject project :  projectManager.getProjects()) {

            List<SBuildType> buildTypes = project.getBuildTypes();
            for (SBuildType buildType : buildTypes) {
                if(!ids.contains(buildType.getInternalId())) {
                    ids.add(buildType.getInternalId());
                    buildConf = new ProjectConfig(buildType.getName(), buildType.getExternalId());
                    list.add(buildConf);
                }
            }
        }

        ProjectConfig[] jobs = list.toArray(new ProjectConfig[list.size()]);
        return new ProjectsList(jobs);
    }

    @Override
    public TreeItem createStructure(String buildConfigurationId) {
        SBuildType root = projectManager.findBuildTypeByExternalId(buildConfigurationId);
        TreeItem treeRoot =null;
        if(root !=null) {
            treeRoot = new TreeItem(root.getName(), root.getExternalId());
            createPipelineStructure(treeRoot, root.getChildDependencies());

        }else{
            //should update the response?
        }
        return treeRoot;
    }

    private void createPipelineStructure(TreeItem treeRoot, Collection<SBuildType> dependencies){
        if(dependencies ==null || dependencies.size() == 0)return;
        TreeItemContainer treeItemContainer = new TreeItemContainer(true,"teamcity_dependencies");
        for(SBuildType build : dependencies){
            TreeItem buildItem = new TreeItem(build.getName(),build.getExternalId());
            treeItemContainer.addJob(buildItem);
            //treeRoot.addChild(buildItem);
            createPipelineStructure(buildItem, build.getChildDependencies());
        }
        treeRoot.addPhasesInternal(treeItemContainer);
    }

}
