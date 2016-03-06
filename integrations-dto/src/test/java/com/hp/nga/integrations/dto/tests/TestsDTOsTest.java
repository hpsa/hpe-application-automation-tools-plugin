package com.hp.nga.integrations.dto.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hp.nga.integrations.dto.DTOFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by gullery on 06/03/2016.
 */

public class TestsDTOsTest {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String ModuleName = "module";
	private static final String PackageName = "package";
	private static final String ClassName = "class";
	private static final String TestName = "test";
	private static final String Result = "Passed";
	private static final int Duration = 3000;
	private static final long Started = System.currentTimeMillis();

	@Test
	public void test_A() throws JsonProcessingException {
		String expected = "<test_run module=\"module\" package=\"package\" class=\"class\" name=\"test\" status=\"Passed\" duration=\"3000\" started=\"" + Started + "\"/>";

		TestRun tr = dtoFactory.newDTO(TestRun.class)
				.setModuleName(ModuleName)
				.setPackageName(PackageName)
				.setClassName(ClassName)
				.setTestName(TestName)
				.setResult(Result)
				.setStarted(Started)
				.setDuration(Duration);

		String xml = dtoFactory.dtoToXml(tr);
		assertNotNull(xml);
		assertEquals(expected, xml);
		TestRun backO = dtoFactory.dtoFromXml(xml, TestRun.class);
		assertNotNull(backO);
		assertEquals(ModuleName, backO.getModuleName());
		assertEquals(PackageName, backO.getPackageName());
		assertEquals(ClassName, backO.getClassName());
		assertEquals(TestName, backO.getTestName());
		assertEquals(Result, backO.getResult());
		assertEquals(Started, backO.getStarted());
		assertEquals(Duration, backO.getDuration());
	}

	@Test
	public void test_B() throws JsonProcessingException {
		String expected = "<test_result><test_runs><test_run module=\"module\" package=\"package\" class=\"class\" name=\"test\" status=\"Passed\" duration=\"3000\" started=\"" + Started + "\"/><test_run module=\"module\" package=\"package\" class=\"class\" name=\"test\" status=\"Passed\" duration=\"3000\" started=\"" + Started + "\"/><test_run module=\"module\" package=\"package\" class=\"class\" name=\"test\" status=\"Passed\" duration=\"3000\" started=\"" + Started + "\"/></test_runs></test_result>";

		TestRun tr1 = dtoFactory.newDTO(TestRun.class)
				.setModuleName(ModuleName)
				.setPackageName(PackageName)
				.setClassName(ClassName)
				.setTestName(TestName)
				.setResult(Result)
				.setStarted(Started)
				.setDuration(Duration);
		TestRun tr2 = dtoFactory.newDTO(TestRun.class)
				.setModuleName(ModuleName)
				.setPackageName(PackageName)
				.setClassName(ClassName)
				.setTestName(TestName)
				.setResult(Result)
				.setStarted(Started)
				.setDuration(Duration);
		TestRun tr3 = dtoFactory.newDTO(TestRun.class)
				.setModuleName(ModuleName)
				.setPackageName(PackageName)
				.setClassName(ClassName)
				.setTestName(TestName)
				.setResult(Result)
				.setStarted(Started)
				.setDuration(Duration);
		TestResult result = dtoFactory.newDTO(TestResult.class)
				.setTestRuns(new TestRun[]{tr1, tr2, tr3});

		ObjectMapper objectMapper = new XmlMapper();
		String xml = objectMapper.writeValueAsString(result);
		assertNotNull(xml);
		assertEquals(expected, xml);
		TestResult backO = dtoFactory.dtoFromXml(xml, TestResult.class);
		assertNotNull(backO);
		assertNotNull(backO.getTestRuns());
		assertEquals(3, backO.getTestRuns().length);
	}
}
