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


    public List<TreeItem> getChildren() {
        return children;
    }

    public void addChild(TreeItem item){
        this.children.add(item);
    }

    public List<ParameterConfig> getParameters() {
        return parameters;
    }

    String name;
    String id;
    List<TreeItem> children = new ArrayList<TreeItem>();
    List<ParameterConfig> parameters = new ArrayList<ParameterConfig>();

    public List<TreeItemContainer> getPhasesInternal() {
        return phasesInternal;
    }

    List<TreeItemContainer> phasesInternal = new ArrayList<TreeItemContainer>();

}
