package com.hpe.application.automation.tools.results.service.almentities;

public interface AlmTest extends AlmEntity{
	public final String TEST_NAME = "name";
	public final String TEST_TYPE = "subtype-id";
	public final String TS_TESTING_FRAMEWORK = "testing-framework";
	public final String TS_TESTING_TOOL = "testing-tool";
	public final String TS_UT_CLASS_NAME = "ut-class-name";
	public final String TS_UT_METHOD_NAME = "ut-method-name";
	public final String TEST_RESPONSIBLE = "owner";
	public final String TS_UT_PACKAGE_NAME = "ut-package-name";
	
	public String getKey();
}
