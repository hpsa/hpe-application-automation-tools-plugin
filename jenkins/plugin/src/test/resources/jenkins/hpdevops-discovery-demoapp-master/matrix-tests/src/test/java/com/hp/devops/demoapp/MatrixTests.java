package com.hp.devops.demoapp;
 
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatrixTests {

	private static String paramA;
	private static String paramB;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		paramA = System.getProperty("paramA");
		paramB = System.getProperty("paramB");
		System.out.println("Properties used: ParamA=" + paramA + "; ParamB=" + paramB);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
    public void testAxisA() {
        Boolean param = Boolean.valueOf(paramA);
		System.out.println("Running test for Axis A " + Thread.currentThread().getStackTrace()[1]);
        Assert.assertTrue(param);
    }
	
	@Test
    public void testAxisB() {
        Boolean param = Boolean.valueOf(paramB);
		System.out.println("Running test for Axis B " + Thread.currentThread().getStackTrace()[1]);
        Assert.assertTrue(param);
    }
}
