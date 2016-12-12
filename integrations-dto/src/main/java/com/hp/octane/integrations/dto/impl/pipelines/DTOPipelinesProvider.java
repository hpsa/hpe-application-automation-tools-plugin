package com.hp.octane.integrations.dto.impl.pipelines;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.api.pipelines.BuildHistory;
import com.hp.octane.integrations.dto.api.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.api.pipelines.PipelinePhase;

/**
 * Created by gullery on 10/02/2016.
 *
 * Pipelines structure related DTOs definitions provider
 */

public final class DTOPipelinesProvider extends DTOInternalProviderBase {

	public DTOPipelinesProvider() {
		dtoPairs.put(PipelineNode.class, PipelineNodeImpl.class);
		dtoPairs.put(PipelinePhase.class, PipelinePhaseImpl.class);
		dtoPairs.put(BuildHistory.class, BuildHistoryImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
