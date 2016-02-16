package com.hp.nga.integrations.dto.coverage;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 29/12/2015.
 */

public interface TestCoverage extends DTOBase {

	String getTestName();

	TestCoverage setTestName(String testName);

	String getTestClass();

	TestCoverage setTestClass(String testClass);

	String getTestPackage();

	TestCoverage setTestPackage(String testPackage);

	String getTestModule();

	TestCoverage setTestModule(String testModule);

	FileCoverage[] getLocs();

	TestCoverage setLocs(FileCoverage[] locs);
}
