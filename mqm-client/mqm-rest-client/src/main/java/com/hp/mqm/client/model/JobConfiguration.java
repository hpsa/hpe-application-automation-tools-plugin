// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final public class JobConfiguration {

    final private List<Pipeline> relatedPipelines;

    public JobConfiguration(List<Pipeline> relatedPipelines) {
        this.relatedPipelines = relatedPipelines;
    }

    public List<Pipeline> getRelatedPipelines() {
        return relatedPipelines;
    }

    public List<Long> getRelatedWorkspaceIds() {
        List<Long> workspaceIds = new LinkedList<Long>();
        for (Pipeline pipeline : relatedPipelines) {
            workspaceIds.add(pipeline.getWorkspaceId());
        }
        return workspaceIds;
    }

    public Map<Long, List<Pipeline>> getRelatedPipelinesMap() {
        Map<Long, List<Pipeline>> ret = new HashMap<Long, List<Pipeline>>();
        for(Pipeline pipeline : relatedPipelines) {
            if (ret.containsKey(pipeline.getWorkspaceId())) {
                ret.get(pipeline.getWorkspaceId()).add(pipeline);
            } else {
                ret.put(pipeline.getWorkspaceId(), new LinkedList<Pipeline>(Arrays.asList(pipeline)));
            }
        }
        return ret;
    }
}
