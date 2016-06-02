package com.hp.application.automation.tools.results.parser;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import com.hp.application.automation.tools.results.parser.testngxml.TestNGXmlReportParserImpl;
import com.hp.application.automation.tools.results.service.almentities.AlmTestSet;

public class TestTestNGXmlReportParserImpl {

	//@Test
	public void testParseTestSets() throws Exception {

		InputStream in = TestTestNGXmlReportParserImpl.class.getResourceAsStream("testng-results.xml");
		TestNGXmlReportParserImpl parser = new TestNGXmlReportParserImpl();
		List<AlmTestSet> testsets = parser.parseTestSets(in, "TestNG", "jenkins");

		Assert.assertEquals(1, testsets.size());
		Assert.assertEquals("Suite1", testsets.get(0).getName());
	}

}
