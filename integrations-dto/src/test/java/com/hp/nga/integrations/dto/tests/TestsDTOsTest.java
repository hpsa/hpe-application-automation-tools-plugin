package com.hp.nga.integrations.dto.tests;

import javax.xml.bind.JAXBException;

import com.hp.nga.integrations.dto.DTOFactory;
import org.junit.Test;

import java.util.Arrays;

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
	private static final TestRunResult Result = TestRunResult.PASSED;
	private static final int Duration = 3000;
	private static final long Started = System.currentTimeMillis();

	@Test
	public void test_A() throws JAXBException {
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
	public void test_B() throws JAXBException {
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
		TestsResult result = dtoFactory.newDTO(TestsResult.class)
				.setTestRuns(Arrays.asList(tr1, tr2, tr3));

		String xml = dtoFactory.dtoToXml(result);
		assertNotNull(xml);
		TestsResult backO = dtoFactory.dtoFromXml(xml, TestsResult.class);
		assertNotNull(backO);
		assertNotNull(backO.getTestRuns());
		assertEquals(3, backO.getTestRuns().size());
	}
}
