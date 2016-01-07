package com.hp.octane.plugins.jetbrains.teamcity.model.pipeline;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.ParameterConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 06/01/2016.
 */
public class StructureItem {

    public StructureItem(String name, String externalId) {
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

    public void addPhasesInternal(StructurePhase phaseInternal) {
        this.phasesInternal.add(phaseInternal);
    }


    public List<StructurePhase> getPhasesInternal() {
        return phasesInternal;
    }

    List<StructurePhase> phasesInternal = new ArrayList<StructurePhase>();

}
