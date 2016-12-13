package com.hp.octane.integrations.dto.tests.impl;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.tests.Property;
import com.hp.octane.integrations.dto.tests.TestCase;
import com.hp.octane.integrations.dto.tests.TestSuite;

/**
 * Created by gullery on 10/02/2016.
 *
 * JUnit report related DTOs definitions provider
 */

public final class DTOJUnitTestsProvider extends DTOInternalProviderBase {

	public DTOJUnitTestsProvider(DTOFactory.DTOConfiguration configuration) {
		dtoPairs.put(Property.class, PropertyImpl.class);
		dtoPairs.put(TestCase.class, TestCaseImpl.class);
		dtoPairs.put(TestSuite.class, TestSuiteImpl.class);

		xmlAbles.add(TestSuiteImpl.class);
		xmlAbles.add(TestCaseImpl.class);
		xmlAbles.add(PropertyImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
