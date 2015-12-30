package com.hp.octane.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 29/12/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCoverage {
	private String testName;
	private String className;
	private String packageName;
	private String moduleName;
	private int[] lines;

	public TestCoverage() {
	}

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
			throw new IllegalArgumentException("coverage data MUST NOT be null");
		}

		this.testName = testName;
		this.className = className;
		this.packageName = packageName;
		this.moduleName = moduleName;
		this.lines = lines.clone();
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public int[] getLines() {
		return lines.clone();
	}

	public void setLines(int[] lines) {
		this.lines = lines == null ? new int[0] : lines.clone();
	}
}
