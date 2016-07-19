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

	int getDuration();

	TestRun setDuration(int duration);

	long getStarted();

	TestRun setStarted(long started);
}
