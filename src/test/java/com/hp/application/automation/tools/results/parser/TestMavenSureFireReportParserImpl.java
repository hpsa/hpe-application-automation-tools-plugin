package com.hp.application.automation.tools.results.parser;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import com.hp.application.automation.tools.results.parser.mavensurefire.MavenSureFireReportParserImpl;
import com.hp.application.automation.tools.results.service.almentities.AlmTestSet;


public class TestMavenSureFireReportParserImpl {

	//@Test
	public void testParseTestSets()throws Exception {
		InputStream in = TestMavenSureFireReportParserImpl.class.getResourceAsStream("MAVENTEST-com.demoapp.demo.AppTest.xml");
		MavenSureFireReportParserImpl parser = new MavenSureFireReportParserImpl();
		List<AlmTestSet> testsets = parser.parseTestSets(in, "JUnit", "Maven");
		assert (testsets.size () == 1);
		AlmTestSet testset = testsets.get(0);
		Assert.assertEquals("com.demoapp.demo.AppTest", testset.getName());
	}

}
