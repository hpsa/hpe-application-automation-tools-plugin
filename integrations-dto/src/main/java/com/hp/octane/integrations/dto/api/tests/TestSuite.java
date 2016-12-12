package com.hp.octane.integrations.dto.api.tests;

import com.hp.octane.integrations.dto.DTOBase;

import java.util.List;

/**
 * Created by lev on 31/05/2016.
 */

public interface TestSuite extends DTOBase {
	List<Property> getProperties();

	TestSuite setProperties(List<Property> properties);

	List<TestCase> getTestCases();

	TestSuite setTestCases(List<TestCase> testCases);
}
