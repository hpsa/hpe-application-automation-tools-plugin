package com.hp.octane.integrations.dto.tests.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.tests.Property;
import com.hp.octane.integrations.dto.tests.TestCase;
import com.hp.octane.integrations.dto.tests.TestSuite;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOJUnitProvider extends DTOInternalProviderBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOJUnitProvider() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[]{TestSuiteImpl.class, TestCaseImpl.class, PropertyImpl.class};
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
		private static final DTOJUnitProvider instance = new DTOJUnitProvider();
	}
}
