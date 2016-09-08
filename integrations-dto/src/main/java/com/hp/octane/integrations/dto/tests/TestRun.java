package com.hp.octane.integrations.dto.tests;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 06/03/2016.
 */

public interface TestRun extends DTOBase {

	String getModuleName();

	TestRun setModuleName(String moduleName);

	String getPackageName();

	TestRun setPackageName(String packageName);

	String getClassName();

	TestRun setClassName(String className);

	String getTestName();

	TestRun setTestName(String testName);

	TestRunResult getResult();

	TestRun setResult(TestRunResult result);

	Long getDuration();

	TestRun setDuration(Long duration);

	Long getStarted();

	TestRun setStarted(Long started);

	TestRunError getError();

	TestRun setError(TestRunError testError);

	String getExternalReportUrl();

	TestRun setExternalReportUrl(String externalReportUrl);
}
