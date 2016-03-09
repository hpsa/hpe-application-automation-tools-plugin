package com.hp.nga.integrations.dto.pipelines.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOInternalProviderBase;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.pipelines.PipelinePhase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOPipelinesProvider extends DTOInternalProviderBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOPipelinesProvider() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[0];
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOInternalProviderBase> registry, ObjectMapper objectMapper) {
		registry.put(PipelineNode.class, INSTANCE_HOLDER.instance);
		registry.put(PipelinePhase.class, INSTANCE_HOLDER.instance);
		registry.put(BuildHistory.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(PipelineNode.class, PipelineNodeImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(PipelinePhase.class, PipelinePhaseImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(BuildHistory.class, BuildHistoryImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(PipelineNode.class, PipelineNodeImpl.class);
		resolver.addMapping(PipelinePhase.class, PipelinePhaseImpl.class);
		resolver.addMapping(BuildHistory.class, BuildHistoryImpl.class);
		SimpleModule module = new SimpleModule();
		module.setAbstractTypes(resolver);
		objectMapper.registerModule(module);
	}

	public <T> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}

	private static final class INSTANCE_HOLDER {
		private static final DTOPipelinesProvider instance = new DTOPipelinesProvider();
	}
}
