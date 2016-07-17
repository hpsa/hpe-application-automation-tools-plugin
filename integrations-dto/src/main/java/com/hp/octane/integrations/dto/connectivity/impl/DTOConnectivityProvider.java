package com.hp.octane.integrations.dto.connectivity.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.connectivity.OctaneRequest;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.hp.octane.integrations.dto.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.connectivity.OctaneTaskAbridged;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOConnectivityProvider extends DTOInternalProviderBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOConnectivityProvider() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[0];
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOInternalProviderBase> registry, ObjectMapper objectMapper) {
		registry.put(OctaneRequest.class, INSTANCE_HOLDER.instance);
		registry.put(OctaneResponse.class, INSTANCE_HOLDER.instance);
		registry.put(OctaneTaskAbridged.class, INSTANCE_HOLDER.instance);
		registry.put(OctaneResultAbridged.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(OctaneRequest.class, OctaneRequestImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(OctaneResponse.class, OctaneResponseImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(OctaneTaskAbridged.class, OctaneTaskAbridgedImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(OctaneResultAbridged.class, OctaneResultAbridgedImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(OctaneRequest.class, OctaneRequestImpl.class);
		resolver.addMapping(OctaneResponse.class, OctaneResponseImpl.class);
		resolver.addMapping(OctaneTaskAbridged.class, OctaneTaskAbridgedImpl.class);
		resolver.addMapping(OctaneResultAbridged.class, OctaneResultAbridgedImpl.class);
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
		private static final DTOConnectivityProvider instance = new DTOConnectivityProvider();
	}
}
