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

    //map of related workspaces and pipelines related to that workspace <workspaceId, List<Pipeline>>
    public Map<Long, List<Pipeline>> getWorkspacePipelinesMap() {
        Map<Long, List<Pipeline>> ret = new HashMap<Long, List<Pipeline>>();
        for (Pipeline pipeline : relatedPipelines) {
            if (ret.containsKey(pipeline.getWorkspaceId())) {
                ret.get(pipeline.getWorkspaceId()).add(pipeline);
            } else {
                ret.put(pipeline.getWorkspaceId(), new LinkedList<Pipeline>(Arrays.asList(pipeline)));
            }
        }
        return ret;
    }
}
