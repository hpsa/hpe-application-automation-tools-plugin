package com.hp.nga.integrations.dto.stormRunner.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOInternalProviderBase;
import com.hp.nga.integrations.dto.stormRunner.Property;
import com.hp.nga.integrations.dto.stormRunner.TestCase;
import com.hp.nga.integrations.dto.stormRunner.TestSuite;



import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOStormRunnerProvider extends DTOInternalProviderBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOStormRunnerProvider() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[]{PropertyImpl.class, TestCaseImpl.class, TestSuiteImpl.class};
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOInternalProviderBase> registry, ObjectMapper objectMapper) {
		registry.put(Property.class, INSTANCE_HOLDER.instance);
		registry.put(TestCase.class, INSTANCE_HOLDER.instance);
		registry.put(TestSuite.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(Property.class, PropertyImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(TestCase.class, TestCaseImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(TestSuite.class, TestSuiteImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(Property.class, PropertyImpl.class);
		resolver.addMapping(TestCase.class, TestCaseImpl.class);
		resolver.addMapping(TestSuite.class, TestSuiteImpl.class);
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
		private static final DTOStormRunnerProvider instance = new DTOStormRunnerProvider();
	}
}
