package com.hp.nga.integrations.dto.connectivity.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOInternalProviderBase;
import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;

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
		registry.put(NGARequest.class, INSTANCE_HOLDER.instance);
		registry.put(NGAResponse.class, INSTANCE_HOLDER.instance);
		registry.put(NGATaskAbridged.class, INSTANCE_HOLDER.instance);
		registry.put(NGAResultAbridged.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(NGARequest.class, NGARequestImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(NGAResponse.class, NGAResponseImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(NGATaskAbridged.class, NGATaskAbridgedImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(NGAResultAbridged.class, NGAResultAbridgedImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(NGARequest.class, NGARequestImpl.class);
		resolver.addMapping(NGAResponse.class, NGAResponseImpl.class);
		resolver.addMapping(NGATaskAbridged.class, NGATaskAbridgedImpl.class);
		resolver.addMapping(NGAResultAbridged.class, NGAResultAbridgedImpl.class);
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
