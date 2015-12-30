package com.hp.octane.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.octane.api.JSONable;
import com.hp.octane.serialization.SerializationService;

/**
 * Created by gullery on 29/12/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRunCoverage implements JSONable {
	private String testName;
	private String file;
	private int[] lines;

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int[] getLines() {
		return lines;
	}

	public void setLines(int[] lines) {
		this.lines = lines;
	}

	@Override
	public String toString() {
		return "TestRunCoverage: { testName: " + testName + " }";
	}

	public String toJSON() throws JsonProcessingException {
		return SerializationService.getObjectMapper().writeValueAsString(this);
	}
}
