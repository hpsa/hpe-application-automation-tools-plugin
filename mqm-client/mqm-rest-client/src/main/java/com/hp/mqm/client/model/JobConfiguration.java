// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client.model;

import java.util.Collections;
import java.util.List;

final public class JobConfiguration {

    final private Integer jobId;
    final private String jobName;
    final private boolean pipelineRoot;
    final private List<Pipeline> relatedPipelines;
    final private List<FieldMetadata> fieldMetadata;

    public JobConfiguration(int jobId, String jobName, boolean pipelineRoot, List<Pipeline> relatedPipelines, List<FieldMetadata> fieldMetadata) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.pipelineRoot = pipelineRoot;
        this.relatedPipelines = relatedPipelines;
        this.fieldMetadata = fieldMetadata;
    }

    public JobConfiguration(boolean pipelineRoot, List<Pipeline> relatedPipelines) {
        this.jobId = null;
        this.jobName = null;
        this.pipelineRoot = pipelineRoot;
        this.relatedPipelines = relatedPipelines;
        this.fieldMetadata = Collections.emptyList();
    }

    public Integer getJobId() {
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

    public List<FieldMetadata> getFieldMetadata() {
        return fieldMetadata;
    }
}
