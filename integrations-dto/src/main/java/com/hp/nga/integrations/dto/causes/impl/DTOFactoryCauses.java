package com.hp.nga.integrations.dto.causes.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOFactoryInternalBase;
import com.hp.nga.integrations.dto.causes.CIEventCause;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOFactoryCauses extends DTOFactoryInternalBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOFactoryCauses() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[0];
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOFactoryInternalBase> registry, ObjectMapper objectMapper) {
		registry.put(CIEventCause.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(CIEventCause.class, CIEventCauseImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(CIEventCause.class, CIEventCauseImpl.class);
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
		private static final DTOFactoryCauses instance = new DTOFactoryCauses();
	}
}
