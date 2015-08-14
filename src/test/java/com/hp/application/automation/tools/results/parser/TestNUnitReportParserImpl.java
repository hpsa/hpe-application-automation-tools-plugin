package com.hp.application.automation.tools.results.parser;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import com.hp.application.automation.tools.results.parser.nunit.NUnitReportParserImpl;
import com.hp.application.automation.tools.results.service.almentities.AlmTestSet;

public class TestNUnitReportParserImpl {

	//@Test
	public void testParseTestSets() throws Exception{
		InputStream in = TestNUnitReportParserImpl.class.getResourceAsStream("NUnitReport.xml");
		NUnitReportParserImpl parser = new NUnitReportParserImpl();
		List<AlmTestSet> testsets = parser.parseTestSets(in, "NUnit", "Selenium");
		Assert.assertEquals(1, testsets.size());
		Assert.assertEquals("NUnit_Test1.dll_ExampleTestOfNUnit", testsets.get(0).getName());
	}

}
