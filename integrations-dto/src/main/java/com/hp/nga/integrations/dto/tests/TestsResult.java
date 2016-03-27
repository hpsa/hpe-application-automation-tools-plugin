package com.hp.nga.integrations.dto.tests;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 06/03/2016.
 */

public interface TestsResult extends DTOBase {

	TestRun[] getTestRuns();

	TestsResult setTestRuns(TestRun[] testRuns);

	BuildContext getBuildContext();

	TestsResult setBuildContext(BuildContext buildContext);
}
