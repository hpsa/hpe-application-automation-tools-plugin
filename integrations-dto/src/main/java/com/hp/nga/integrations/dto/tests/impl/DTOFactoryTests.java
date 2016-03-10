package com.hp.nga.integrations.dto.tests.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOFactoryInternalBase;
import com.hp.nga.integrations.dto.tests.BuildContext;
import com.hp.nga.integrations.dto.tests.TestResult;
import com.hp.nga.integrations.dto.tests.TestRun;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOFactoryTests extends DTOFactoryInternalBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOFactoryTests() {
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOFactoryInternalBase> registry, ObjectMapper objectMapper) {
		registry.put(TestRun.class, INSTANCE_HOLDER.instance);
		registry.put(BuildContext.class, INSTANCE_HOLDER.instance);
		registry.put(TestResult.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(TestRun.class, TestRunImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(TestResult.class, TestResultImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(BuildContext.class, BuildContextImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(TestRun.class, TestRunImpl.class);
		resolver.addMapping(TestResult.class, TestResultImpl.class);
		resolver.addMapping(BuildContext.class, BuildContextImpl.class);
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

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[]{TestRunImpl.class, TestResultImpl.class, BuildContextImpl.class};
	}

	private static final class INSTANCE_HOLDER {
		private static final DTOFactoryTests instance = new DTOFactoryTests();
	}
}
