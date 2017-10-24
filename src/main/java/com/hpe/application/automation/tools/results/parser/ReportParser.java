package com.hpe.application.automation.tools.results.parser;

import java.io.InputStream;
import java.util.List;

import com.hpe.application.automation.tools.results.service.almentities.AlmTestSet;

public interface ReportParser {
	String TESTING_FRAMEWORK_JUNIT = "JUnit";
	String EXTERNAL_TEST_TYPE = "EXTERNAL-TEST";
	String REPORT_FORMAT_JENKINS_JUNIT_PLUGIN = "Jenkins JUnit Plugin";
	String REPORT_FORMAT_ANT = "Ant";
	String REPORT_FORMAT_MAVEN_SUREFIRE_PLUGIN = "Maven Surefire Plugin";
	String EXTERNAL_TEST_SET_TYPE_ID = "hp.qc.test-set.external";
	String EXTERNAL_TEST_INSTANCE_TYPE_ID = "hp.qc.test-instance.external-test";
	String EXTERNAL_RUN_TYPE_ID = "hp.qc.run.external-test";
	
	List<AlmTestSet> parseTestSets(InputStream reportInputStream, String testingFramework, String testingTool) throws ReportParseException;
}
