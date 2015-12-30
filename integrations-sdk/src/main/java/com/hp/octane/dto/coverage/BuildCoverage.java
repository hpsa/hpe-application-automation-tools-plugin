package com.hp.octane.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 30/12/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildCoverage {
	private TestCoverage[] testCoverages;

	public BuildCoverage() {
	}

	public BuildCoverage(TestCoverage[] testCoverages) {
		if (testCoverages == null) {
			throw new IllegalArgumentException("coverage data MUST NOT be null");
		}

		this.testCoverages = testCoverages.clone();
	}

	public TestCoverage[] getTestCoverages() {
		return testCoverages;
	}

	public void setTestCoverages(TestCoverage[] testCoverages) {
		this.testCoverages = testCoverages;
	}
}
