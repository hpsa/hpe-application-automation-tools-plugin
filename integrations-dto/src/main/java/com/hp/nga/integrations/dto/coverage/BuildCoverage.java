package com.hp.nga.integrations.dto.coverage;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 30/12/2015.
 */

public interface BuildCoverage extends DTOBase {

	TestCoverage[] getTestCoverages();

	BuildCoverage setTestCoverages(TestCoverage[] testCoverages);
}
