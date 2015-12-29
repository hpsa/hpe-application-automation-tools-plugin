package com.hp.octane.plugins.jetbrains.teamcity.model.api;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 24/12/2015.
 */
public class ProjectsList {

    public ProjectConfig[] jobs;

    public ProjectConfig[] getJobs() {
        return jobs;
    }

    public ProjectsList(boolean areParametersNeeded, ProjectManager projectManager) {

        List<ProjectConfig> list = new ArrayList<ProjectConfig>();
        //todo: change to project
        ProjectConfig buildConf;
        for (SProject project :  projectManager.getProjects()) {

            List<SBuildType> buildTypes = project.getBuildTypes();
            for (SBuildType buildType : buildTypes) {

                SBuild latestBuild = buildType.getLastChangesStartedBuild();
                if (latestBuild != null) {
                    buildConf = new ProjectConfig(latestBuild.getBuildTypeName(), latestBuild.getBuildTypeExternalId());
                    list.add(buildConf);
                }
            }
        }
        jobs = list.toArray(new ProjectConfig[list.size()]);
    }
}
