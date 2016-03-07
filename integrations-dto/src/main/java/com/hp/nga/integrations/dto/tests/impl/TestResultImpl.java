package com.hp.nga.integrations.dto.tests.impl;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.hp.nga.integrations.dto.tests.TestResult;
import com.hp.nga.integrations.dto.tests.TestRun;

/**
 * Created by gullery on 06/03/2016.
 */

@JacksonXmlRootElement(localName = "test_result")
class TestResultImpl implements TestResult {
	private TestRun[] testRuns;

	@JacksonXmlElementWrapper(localName = "test_runs")
	@JacksonXmlProperty(localName = "test_run")
	public TestRun[] getTestRuns() {
		return testRuns;
	}

	public TestResult setTestRuns(TestRun[] testRuns) {
		this.testRuns = testRuns;
		return this;
	}
}
