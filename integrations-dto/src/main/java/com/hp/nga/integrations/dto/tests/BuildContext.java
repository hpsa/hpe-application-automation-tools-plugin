package com.hp.nga.integrations.dto.tests;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 06/03/2016.
 */

public interface BuildContext extends DTOBase {

	String getModuleName();

	BuildContext setModuleName(String moduleName);

	String getPackageName();

	BuildContext setPackageName(String packageName);

	String getClassName();

	BuildContext setClassName(String className);

	String getTestName();

	BuildContext setTestName(String testName);

	TestRunResult getResult();

	BuildContext setResult(TestRunResult result);

	int getDuration();

	BuildContext setDuration(int duration);

	long getStarted();

	BuildContext setStarted(long started);
}
