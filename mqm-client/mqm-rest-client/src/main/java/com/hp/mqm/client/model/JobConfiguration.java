// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

import java.util.List;

final public class JobConfiguration {

    final private List<Pipeline> relatedPipelines;

    public JobConfiguration(List<Pipeline> relatedPipelines) {
        this.relatedPipelines = relatedPipelines;
    }

    public List<Pipeline> getRelatedPipelines() {
        return relatedPipelines;
    }
}
