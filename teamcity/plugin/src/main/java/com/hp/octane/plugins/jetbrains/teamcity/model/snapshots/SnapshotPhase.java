package com.hp.octane.plugins.jetbrains.teamcity.model.snapshots;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.AbstractPhase;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 21/01/2016.
 */
public class SnapshotPhase extends AbstractPhase{

    List<StructureItem> builds = new ArrayList<StructureItem>();

    public SnapshotPhase(boolean blocking, String name) { super(blocking,name);}

    public List<StructureItem> getBuilds() {return builds;}

    public void addBuilds(StructureItem build){
        builds.add(build);
    }
}
