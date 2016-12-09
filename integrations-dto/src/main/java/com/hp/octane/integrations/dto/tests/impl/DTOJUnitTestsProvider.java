package com.hp.octane.integrations.dto.tests.impl;

import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.tests.Property;
import com.hp.octane.integrations.dto.tests.TestCase;
import com.hp.octane.integrations.dto.tests.TestSuite;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 10/02/2016.
 *
 * JUnit report related DTOs definitions provider
 */

public final class DTOJUnitTestsProvider extends DTOInternalProviderBase {
	private final Map<Class<? extends DTOBase>, Class> dtoPairs = new HashMap<>();

	public DTOJUnitTestsProvider() {
		dtoPairs.put(Property.class, PropertyImpl.class);
		dtoPairs.put(TestCase.class, TestCaseImpl.class);
		dtoPairs.put(TestSuite.class, TestSuiteImpl.class);
	}

	@Override
	protected void provideImplResolvingMap(SimpleAbstractTypeResolver dtoImplResolver) {
		dtoImplResolver.addMapping(Property.class, PropertyImpl.class);
		dtoImplResolver.addMapping(TestCase.class, TestCaseImpl.class);
		dtoImplResolver.addMapping(TestSuite.class, TestSuiteImpl.class);
	}

	@Override
	protected Set<Class<? extends DTOBase>> getJSONAbleDTOs() {
		return dtoPairs.keySet();
	}

	@Override
	protected Class[] getXMLAbleDTOs() {
		return new Class[]{TestSuiteImpl.class, TestCaseImpl.class, PropertyImpl.class};
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
