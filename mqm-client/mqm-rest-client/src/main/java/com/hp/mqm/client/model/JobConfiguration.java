// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

import java.util.List;

final public class JobConfiguration {

    final private int jobId;
    final private String jobName;
    final private boolean pipelineRoot;
    final private List<Pipeline> relatedPipelines;

    public JobConfiguration(int jobId, String jobName, boolean pipelineRoot, List<Pipeline> relatedPipelines) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.pipelineRoot = pipelineRoot;
        this.relatedPipelines = relatedPipelines;
    }

    public int getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public boolean isPipelineRoot() {
        return pipelineRoot;
    }

    public List<Pipeline> getRelatedPipelines() {
        return relatedPipelines;
    }
}
