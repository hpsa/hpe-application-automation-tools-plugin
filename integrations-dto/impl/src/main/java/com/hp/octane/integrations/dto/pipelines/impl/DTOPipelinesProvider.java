package com.hp.octane.integrations.dto.pipelines.impl;

import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.pipelines.BuildHistory;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.pipelines.PipelinePhase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 10/02/2016.
 *
 * Pipelines structure related DTOs definitions provider
 */

public final class DTOPipelinesProvider extends DTOInternalProviderBase {
	private final Map<Class<? extends DTOBase>, Class> dtoPairs = new HashMap<>();

	public DTOPipelinesProvider() {
		dtoPairs.put(PipelineNode.class, PipelineNodeImpl.class);
		dtoPairs.put(PipelinePhase.class, PipelinePhaseImpl.class);
		dtoPairs.put(BuildHistory.class, BuildHistoryImpl.class);
	}

	@Override
	protected void provideImplResolvingMap(SimpleAbstractTypeResolver dtoImplResolver) {
		dtoImplResolver.addMapping(PipelineNode.class, PipelineNodeImpl.class);
		dtoImplResolver.addMapping(PipelinePhase.class, PipelinePhaseImpl.class);
		dtoImplResolver.addMapping(BuildHistory.class, BuildHistoryImpl.class);
	}

	@Override
	protected Set<Class<? extends DTOBase>> getJSONAbleDTOs() {
		return dtoPairs.keySet();
	}

	@Override
	protected Class[] getXMLAbleDTOs() {
		return new Class[0];
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
