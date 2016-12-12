package com.hp.octane.integrations.dto.api.coverage;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 30/12/2015.
 */

public interface BuildCoverage extends DTOBase {

	TestCoverage[] getTestCoverages();

	BuildCoverage setTestCoverages(TestCoverage[] testCoverages);
}
