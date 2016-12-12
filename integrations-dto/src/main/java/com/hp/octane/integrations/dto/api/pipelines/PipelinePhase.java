package com.hp.octane.integrations.dto.api.pipelines;

import com.hp.octane.integrations.dto.DTOBase;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

public interface PipelinePhase extends DTOBase {

	String getName();

	PipelinePhase setName(String name);

	boolean isBlocking();

	PipelinePhase setBlocking(boolean blocking);

	List<PipelineNode> getJobs();

	PipelinePhase setJobs(List<PipelineNode> jobs);
}