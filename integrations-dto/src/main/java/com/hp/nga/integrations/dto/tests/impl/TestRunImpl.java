package com.hp.nga.integrations.dto.tests.impl;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.hp.nga.integrations.dto.tests.TestRun;

/**
 * Created by gullery on 06/03/2016.
 */

@JacksonXmlRootElement(localName = "test_run")
class TestRunImpl implements TestRun {
	private String moduleName;
	private String packageName;
	private String className;
	private String testName;
	private String result;
	private int duration;
	private long started;

	@JacksonXmlProperty(isAttribute = true, localName = "module")
	public String getModuleName() {
		return moduleName;
	}

	public TestRun setModuleName(String moduleName) {
		this.moduleName = moduleName;
		return this;
	}

	@JacksonXmlProperty(isAttribute = true, localName = "package")
	public String getPackageName() {
		return packageName;
	}

	public TestRun setPackageName(String packageName) {
		this.packageName = packageName;
		return this;
	}

	@JacksonXmlProperty(isAttribute = true, localName = "class")
	public String getClassName() {
		return className;
	}

	public TestRun setClassName(String className) {
		this.className = className;
		return this;
	}

	@JacksonXmlProperty(isAttribute = true, localName = "name")
	public String getTestName() {
		return testName;
	}

	public TestRun setTestName(String testName) {
		this.testName = testName;
		return this;
	}

	@JacksonXmlProperty(isAttribute = true, localName = "status")
	public String getResult() {
		return result;
	}

	public TestRun setResult(String result) {
		this.result = result;
		return this;
	}

	@JacksonXmlProperty(isAttribute = true, localName = "duration")
	public int getDuration() {
		return duration;
	}

	public TestRun setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	@JacksonXmlProperty(isAttribute = true, localName = "started")
	public long getStarted() {
		return started;
	}

	public TestRun setStarted(long started) {
		this.started = started;
		return this;
	}
}
