package com.hp.octane.plugins.jetbrains.teamcity.model.pipeline;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.AbstractPhase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 06/01/2016.
 */
public class StructurePhase extends AbstractPhase {
    List<StructureItem> jobs = new ArrayList<StructureItem>();

    public StructurePhase(boolean blocking, String name) {
        super(blocking,name);

    }

    public List<StructureItem> getJobs() {
        return jobs;
    }

    public void addJob(StructureItem job){
        jobs.add(job);
    }
}
