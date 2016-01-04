package com.hp.octane.plugins.jetbrains.teamcity.model.api;

/**
 * Created by lazara on 24/12/2015.
 */
public class ProjectsList {

    public ProjectConfig[] jobs;

    public ProjectsList(ProjectConfig[] jobs) {
        this.jobs = jobs;
    }

    public ProjectConfig[] getJobs() {
        return jobs;
    }
}
