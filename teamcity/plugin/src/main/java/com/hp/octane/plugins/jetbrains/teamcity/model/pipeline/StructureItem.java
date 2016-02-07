package com.hp.octane.plugins.jetbrains.teamcity.model.pipeline;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.octane.plugins.jetbrains.teamcity.model.api.AbstractPhase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazara on 06/01/2016.
 */
public class StructureItem{

    public StructureItem(String name, String externalId) {
        this.name = name;
        this.ciId = externalId;
    }

    public String getName() {
        return name;
    }

    public String getCiId() {
        return ciId;
    }

    public List<ParameterConfig> getParameters() {
        return parameters;
    }

    String name;
    String ciId;
    List<ParameterConfig> parameters = new ArrayList<ParameterConfig>();

    public void addPhasesInternal(AbstractPhase phaseInternal) {
        this.phasesInternal.add(phaseInternal);
    }


    public List<AbstractPhase> getPhasesInternal() {
        return phasesInternal;
    }

    List<AbstractPhase> phasesInternal = new ArrayList<AbstractPhase>();

}
