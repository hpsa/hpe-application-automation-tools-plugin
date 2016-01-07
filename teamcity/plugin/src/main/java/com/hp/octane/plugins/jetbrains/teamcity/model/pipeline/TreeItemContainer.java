package com.hp.octane.plugins.jetbrains.teamcity.model.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 06/01/2016.
 */
public class TreeItemContainer {
    private boolean blocking =false;
    private String name;
    List<TreeItem> jobs = new ArrayList<TreeItem>();

    public TreeItemContainer(boolean blocking, String name) {
        this.blocking = blocking;
        this.name = name;
//        this.jobs = jobs;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public String getName() {
        return name;
    }

    public List<TreeItem> getJobs() {
        return jobs;
    }

    public void addJob(TreeItem job){
        jobs.add(job);
    }
}
