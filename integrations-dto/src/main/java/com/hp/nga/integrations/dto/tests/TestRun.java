package com.hp.nga.integrations.dto.tests;

import com.hp.nga.integrations.dto.DTOBase;

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

	String getResult();

	TestRun setResult(String result);

	int getDuration();

	TestRun setDuration(int duration);

	long getStarted();

	TestRun setStarted(long started);
}
