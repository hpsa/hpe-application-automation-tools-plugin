package com.hp.nga.integrations.dto.tests.impl;

import com.hp.nga.integrations.dto.tests.TestResult;
import com.hp.nga.integrations.dto.tests.TestRun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by gullery on 06/03/2016.
 */

@XmlRootElement(name = "test_result")
@XmlAccessorType(XmlAccessType.NONE)
class TestResultImpl implements TestResult {

	@XmlElementWrapper(name = "test_runs")
	@XmlAnyElement(lax = true)
	private TestRun[] testRuns;

	public TestRun[] getTestRuns() {
		return testRuns;
	}

	public TestResult setTestRuns(TestRun[] testRuns) {
		this.testRuns = testRuns;
		return this;
	}
}
