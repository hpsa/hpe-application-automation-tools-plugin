package com.hp.application.automation.tools.results.parser;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hp.application.automation.tools.results.parser.jenkinsjunit.JenkinsJUnitReportParserImpl;
import com.hp.application.automation.tools.results.service.almentities.AlmTestSet;

public class TestJenkinsJunitReportParserImpl {

	//@Test
	public void testParseTestSets() throws Exception {
		InputStream in = TestJenkinsJunitReportParserImpl.class.getResourceAsStream("junitResult.xml");
		JenkinsJUnitReportParserImpl parser = new JenkinsJUnitReportParserImpl();
		List<AlmTestSet> testsets = parser.parseTestSets(in, "JUnit", "LeanFT");
		Assert.assertEquals(1, testsets.size());
		Assert.assertEquals("prog1prj1.TestProg1Prj1Test1", testsets.get(0).getName());
	}

}
