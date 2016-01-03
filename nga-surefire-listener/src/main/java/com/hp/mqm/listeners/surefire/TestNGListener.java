/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.hp.mqm.listeners.surefire;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;

/**
 * TestNG and JUnit listener that instructs JaCoCo to create one session per test.
 */
public class TestNGListener extends JUnitListener implements ITestListener {

	public TestNGListener() {
		this(JacocoController.getInstance());
	}

	TestNGListener(JacocoController jacoco) {
		super(jacoco);
	}


	public void onTestStart(ITestResult result) {
		jacoco.onTestStart(getName(result));
	}

	private static String getName(ITestResult result) {
		if (result != null) {
			return result.getTestClass()
					+ " " + result.getMethod();
		} else {
			return "";
		}
	}


	public void onTestSuccess(ITestResult result) {
		testFinished(result);
	}

	private void testFinished(ITestResult result) {
		String execFileName = result.getTestClass() + "["
				+ result.getMethod() + "]";
		jacoco.onTestFinish(execFileName); // dump results to test .exec file
	}

	public void onTestFailure(ITestResult result) {
		testFinished(result);
	}


	public void onTestSkipped(ITestResult result) {
		testFinished(result);
	}


	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		testFinished(result);
	}


	public void onStart(ITestContext context) {
		// create output directory
		File directory = new File(Paths.REPORT_DIRECTORY_PATH);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}


	public void onFinish(ITestContext context) {
		// nop
	}

}
