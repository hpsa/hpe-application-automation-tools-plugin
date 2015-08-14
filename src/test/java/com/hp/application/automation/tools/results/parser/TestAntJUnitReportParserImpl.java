package com.hp.application.automation.tools.results.parser;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import com.hp.application.automation.tools.results.parser.antjunit.AntJUnitReportParserImpl;
import com.hp.application.automation.tools.results.service.almentities.AlmTestSet;

public class TestAntJUnitReportParserImpl {

	//@Test
	public void testParseTestSets() throws Exception {
		InputStream in = TestAntJUnitReportParserImpl.class.getResourceAsStream("AntTESTS-TestSuites.xml");
		AntJUnitReportParserImpl parser = new AntJUnitReportParserImpl();
		List<AlmTestSet> testsets = parser.parseTestSets(in, "JUnit", "Ant");
		Assert.assertEquals(testsets.size(), 1);
		Assert.assertEquals("TestProg1Prj1Test1", testsets.get(0).getName()  );
	}   

}
