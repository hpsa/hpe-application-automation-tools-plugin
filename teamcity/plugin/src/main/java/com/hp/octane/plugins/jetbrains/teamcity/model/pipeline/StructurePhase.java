package com.hp.octane.plugins.jetbrains.teamcity.model.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 06/01/2016.
 */
public class StructurePhase {
    private boolean blocking =false;
    private String name;
    List<StructureItem> jobs = new ArrayList<StructureItem>();

    public StructurePhase(boolean blocking, String name) {
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

    public List<StructureItem> getJobs() {
        return jobs;
    }

    public void addJob(StructureItem job){
        jobs.add(job);
    }
}
