package com.hp.nga.integrations.dto.pipelines;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

public interface PipelinePhase {

	String getName();

	PipelinePhase setName(String name);

	boolean isBlocking();

	PipelinePhase setBlocking(boolean blocking);

	List<PipelineItem> getJobs();

	PipelinePhase setJobs(List<PipelineItem> jobs);
}