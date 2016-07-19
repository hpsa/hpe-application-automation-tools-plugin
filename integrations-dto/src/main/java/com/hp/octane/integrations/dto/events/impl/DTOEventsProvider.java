package com.hp.octane.integrations.dto.events.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventsList;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOEventsProvider extends DTOInternalProviderBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOEventsProvider() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[0];
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOInternalProviderBase> registry, ObjectMapper objectMapper) {
		registry.put(CIEvent.class, INSTANCE_HOLDER.instance);
		registry.put(CIEventsList.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(CIEvent.class, CIEventImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(CIEventsList.class, CIEventsListImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(CIEvent.class, CIEventImpl.class);
		resolver.addMapping(CIEventsList.class, CIEventsListImpl.class);
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
		private static final DTOEventsProvider instance = new DTOEventsProvider();
	}
}
