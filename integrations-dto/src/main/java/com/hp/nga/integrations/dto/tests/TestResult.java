package com.hp.nga.integrations.dto.tests;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 06/03/2016.
 */

public interface TestResult extends DTOBase {

	TestRun[] getTestRuns();

	TestResult setTestRuns(TestRun[] testRuns);
}
