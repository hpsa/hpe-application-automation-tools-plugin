package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectConfig;
import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectsList;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 04/01/2016.
 */
public class TeamCityModelFactory implements ModelFactory {

    private static TeamCityModelFactory instance =null;

    public static TeamCityModelFactory getInstance() {
        if(instance == null) {
            instance = new TeamCityModelFactory();
        }
        return instance;
    }

    @Override
    public ProjectsList CreateProjectList(ProjectManager projectManager) {

        List<ProjectConfig> list = new ArrayList<ProjectConfig>();
        List<String>ids = new ArrayList<String>();
        //todo: change to project
        ProjectConfig buildConf;
        for (SProject project :  projectManager.getProjects()) {

            List<SBuildType> buildTypes = project.getBuildTypes();
            for (SBuildType buildType : buildTypes) {
                if(!ids.contains(buildType.getInternalId())) {
                    ids.add(buildType.getInternalId());
                    SBuild latestBuild = buildType.getLastChangesStartedBuild();
                    if (latestBuild != null) {
                        buildConf = new ProjectConfig(latestBuild.getBuildTypeName(), latestBuild.getBuildTypeExternalId());
                        list.add(buildConf);
                    }
                }
            }
        }

        ProjectConfig[] jobs = list.toArray(new ProjectConfig[list.size()]);
        return new ProjectsList(jobs);
    }

}
