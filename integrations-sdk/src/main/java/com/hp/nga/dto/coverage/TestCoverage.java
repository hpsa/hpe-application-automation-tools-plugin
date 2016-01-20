package com.hp.nga.dto.coverage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 29/12/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCoverage {
	private String testName;
	private String testClass;
	private String testPackage;
	private String testModule;
	private FileCoverage[] locs = new FileCoverage[0];

	public TestCoverage() {
	}

	public TestCoverage(String testName, String testClass, String testPackage, String testModule, FileCoverage[] locs) {
		if (testName == null || testName.isEmpty()) {
			throw new IllegalArgumentException("test name MUST NOT be null nor empty");
		}
		if (testClass == null || testClass.isEmpty()) {
			throw new IllegalArgumentException("test class MUST NOT be null nor empty");
		}
		if (testPackage == null || testPackage.isEmpty()) {
			throw new IllegalArgumentException("test package MUST NOT be null nor empty");
		}
		if (testModule == null || testModule.isEmpty()) {
			throw new IllegalArgumentException("test module MUST NOT be null nor empty");
		}
		if (locs == null) {
			throw new IllegalArgumentException("coverage data MUST NOT be null");
		}

		this.testName = testName;
		this.testClass = testClass;
		this.testPackage = testPackage;
		this.testModule = testModule;
		this.locs = locs.clone();
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getTestClass() {
		return testClass;
	}

	public void setTestClass(String testClass) {
		this.testClass = testClass;
	}

	public String getTestPackage() {
		return testPackage;
	}

	public void setTestPackage(String testPackage) {
		this.testPackage = testPackage;
	}

	public String getTestModule() {
		return testModule;
	}

	public void setTestModule(String testModule) {
		this.testModule = testModule;
	}

	public FileCoverage[] getLocs() {
		return locs.clone();
	}

	public void setLocs(FileCoverage[] locs) {
		this.locs = locs == null ? new FileCoverage[0] : locs.clone();
	}
}
