package com.hp.octane.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.octane.api.JSONable;
import com.hp.octane.serialization.SerializationService;

/**
 * Created by gullery on 29/12/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCoverage implements JSONable {
	private final String testName;
	private final String className;
	private final String packageName;
	private final String moduleName;
	private final int[] lines;

	public TestCoverage(String testName, String className, String packageName, String moduleName, int[] lines) {
		if (testName == null || testName.isEmpty()) {
			throw new IllegalArgumentException("test name MUST NOT be null nor empty");
		}
		if (className == null || className.isEmpty()) {
			throw new IllegalArgumentException("class name MUST NOT be null nor empty");
		}
		if (packageName == null || packageName.isEmpty()) {
			throw new IllegalArgumentException("package name MUST NOT be null nor empty");
		}
		if (moduleName == null || moduleName.isEmpty()) {
			throw new IllegalArgumentException("module name MUST NOT be null nor empty");
		}
		if (lines == null) {
			throw new IllegalArgumentException("lines data MUST NOT be null");
		}

		this.testName = testName;
		this.className = className;
		this.packageName = packageName;
		this.moduleName = moduleName;
		this.lines = lines;
	}

	public String getTestName() {
		return testName;
	}

	public String getClassName() {
		return className;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public int[] getLines() {
		return lines.clone();
	}

	@JsonIgnore
	public String toJSON() throws JsonProcessingException {
		return SerializationService.getObjectMapper().writeValueAsString(this);
	}
}
