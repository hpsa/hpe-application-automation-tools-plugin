package com.hp.octane.integrations.dto.tests.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.tests.BuildContext;
import com.hp.octane.integrations.dto.tests.TestsResult;
import com.hp.octane.integrations.dto.tests.TestRun;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOTestsProvider extends DTOInternalProviderBase {
	private final Map<Class, Class> dtoPairs = new HashMap<>();

	private DTOTestsProvider() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[]{BuildContextImpl.class, TestRunImpl.class, TestsResultImpl.class};
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOInternalProviderBase> registry, ObjectMapper objectMapper) {
		registry.put(TestRun.class, INSTANCE_HOLDER.instance);
		registry.put(BuildContext.class, INSTANCE_HOLDER.instance);
		registry.put(TestsResult.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(TestRun.class, TestRunImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(BuildContext.class, BuildContextImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(TestsResult.class, TestsResultImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(TestRun.class, TestRunImpl.class);
		resolver.addMapping(BuildContext.class, BuildContextImpl.class);
		resolver.addMapping(TestsResult.class, TestsResultImpl.class);
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
		private static final DTOTestsProvider instance = new DTOTestsProvider();
	}
}
