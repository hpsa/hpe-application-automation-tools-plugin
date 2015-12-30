package com.hp.octane.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.octane.api.JSONable;
import com.hp.octane.serialization.SerializationService;

/**
 * Created by gullery on 30/12/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildCoverage implements JSONable {
	private final TestCoverage[] testCoverages;

	public BuildCoverage(TestCoverage[] testCoverages) {
		if (testCoverages == null) {
			throw new IllegalArgumentException("coverage data MUST NOT be null");
		}

		this.testCoverages = testCoverages.clone();
	}

	public TestCoverage[] getTestCoverages() {
		return testCoverages.clone();
	}

	@JsonIgnore
	public String toJSON() throws JsonProcessingException {
		return SerializationService.getObjectMapper().writeValueAsString(this);
	}
}
