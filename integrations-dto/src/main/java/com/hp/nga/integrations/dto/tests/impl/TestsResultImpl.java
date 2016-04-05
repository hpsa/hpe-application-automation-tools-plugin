package com.hp.nga.integrations.dto.tests.impl;

import com.hp.nga.integrations.dto.tests.BuildContext;
import com.hp.nga.integrations.dto.tests.TestsResult;
import com.hp.nga.integrations.dto.tests.TestRun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by gullery on 06/03/2016.
 */

@XmlRootElement(name = "test_result")
@XmlAccessorType(XmlAccessType.NONE)
class TestsResultImpl implements TestsResult {

	@XmlAnyElement(lax = true)
	private BuildContext buildContext;

	@XmlElementWrapper(name = "test_runs")
	@XmlAnyElement(lax = true)
	private List<TestRun> testRuns;

	public BuildContext getBuildContext() {
		return buildContext;
	}

	public TestsResult setBuildContext(BuildContext buildContext) {
		this.buildContext = buildContext;
		return this;
	}

	public List<TestRun> getTestRuns() {
		return testRuns;
	}

	public TestsResult setTestRuns(List<TestRun> testRuns) {
		this.testRuns = testRuns;
		return this;
	}
}
