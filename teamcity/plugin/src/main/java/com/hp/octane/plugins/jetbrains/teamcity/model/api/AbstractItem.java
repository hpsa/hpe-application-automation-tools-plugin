package com.hp.octane.plugins.jetbrains.teamcity.model.api;

import com.hp.octane.dto.parameters.ParameterConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 19/01/2016.
 */
public class AbstractItem {
    protected String name;
    protected String id;
    protected List<ParameterConfig> parameters = new ArrayList<ParameterConfig>();


    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
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
}
