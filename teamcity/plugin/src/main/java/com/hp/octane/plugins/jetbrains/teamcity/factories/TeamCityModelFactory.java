package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectConfig;
import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectsList;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructurePhase;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
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

}
