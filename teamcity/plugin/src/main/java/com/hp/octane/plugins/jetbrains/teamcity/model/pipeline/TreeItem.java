package com.hp.octane.plugins.jetbrains.teamcity.model.pipeline;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.ParameterConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 06/01/2016.
 */
public class TreeItem {

    public TreeItem(String name, String externalId) {
        this.name = name;
        this.id = externalId;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<ParameterConfig> getParameters() {
        return parameters;
    }

    String name;
    String id;
    List<ParameterConfig> parameters = new ArrayList<ParameterConfig>();

    public void addPhasesInternal(TreeItemContainer phaseInternal) {
        this.phasesInternal.add(phaseInternal);
    }


    public List<TreeItemContainer> getPhasesInternal() {
        return phasesInternal;
    }

    List<TreeItemContainer> phasesInternal = new ArrayList<TreeItemContainer>();

}
