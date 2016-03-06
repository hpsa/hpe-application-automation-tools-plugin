package com.hp.nga.integrations.dto.coverage.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.coverage.BuildCoverage;
import com.hp.nga.integrations.dto.coverage.TestCoverage;

/**
 * Created by gullery on 30/12/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class BuildCoverageImpl implements BuildCoverage {
	private TestCoverage[] testCoverages;

	public TestCoverage[] getTestCoverages() {
		return testCoverages;
	}

	public BuildCoverage setTestCoverages(TestCoverage[] testCoverages) {
		this.testCoverages = testCoverages;
		return this;
	}
}
