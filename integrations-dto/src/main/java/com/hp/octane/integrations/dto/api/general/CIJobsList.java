package com.hp.octane.integrations.dto.api.general;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.api.pipelines.PipelineNode;

/**
 * Created by gullery on 06/01/2016.
 * <p/>
 * CI Jobs list container descriptor
 */

public interface CIJobsList extends DTOBase {

	PipelineNode[] getJobs();

	CIJobsList setJobs(PipelineNode[] jobs);
}
