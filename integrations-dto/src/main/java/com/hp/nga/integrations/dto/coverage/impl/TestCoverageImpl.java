package com.hp.nga.integrations.dto.coverage.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.coverage.FileCoverage;
import com.hp.nga.integrations.dto.coverage.TestCoverage;

/**
 * Created by gullery on 29/12/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class TestCoverageImpl implements TestCoverage {
	private String testName;
	private String testClass;
	private String testPackage;
	private String testModule;
	private FileCoverage[] locs = new FileCoverage[0];

	public String getTestName() {
		return testName;
	}

	public TestCoverage setTestName(String testName) {
		this.testName = testName;
		return this;
	}

	public String getTestClass() {
		return testClass;
	}

	public TestCoverage setTestClass(String testClass) {
		this.testClass = testClass;
		return this;
	}

	public String getTestPackage() {
		return testPackage;
	}

	public TestCoverage setTestPackage(String testPackage) {
		this.testPackage = testPackage;
		return this;
	}

	public String getTestModule() {
		return testModule;
	}

	public TestCoverage setTestModule(String testModule) {
		this.testModule = testModule;
		return this;
	}

	public FileCoverage[] getLocs() {
		return locs;
	}

	public TestCoverage setLocs(FileCoverage[] locs) {
		this.locs = locs;
		return this;
	}
}
