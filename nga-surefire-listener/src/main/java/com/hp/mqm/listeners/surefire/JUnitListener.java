package com.hp.mqm.listeners.surefire;
/**
 * Created by vaingart on 14/12/2015.
 */

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import java.io.File;

public class JUnitListener extends RunListener {
	protected final JacocoController jacoco;

	public JUnitListener() {
		this(JacocoController.getInstance());
	}

	JUnitListener(JacocoController jacoco) {
		this.jacoco = jacoco;
	}

	/**
	 * Called when an atomic test is about to be started.
	 */
	@Override
	public void testStarted(Description description) {
		String execFileName = description.getClassName() + "[" + description.getMethodName() + "]";
		jacoco.onTestStart(execFileName);
	}

	/**
	 * Called when test run is started
	 *
	 * @param description
	 * @throws Exception
	 */
	@Override
	public void testRunStarted(Description description) throws Exception {
		// create output directory
		Paths.TARGET_DIRECTORY_PATH = System.getProperty("user.dir") + File.separator + "target" + File.separator;
		Paths.REPORT_DIRECTORY_PATH = Paths.TARGET_DIRECTORY_PATH + Paths.REPORT_DIRECTORY_NAME;
		File directory = new File(Paths.REPORT_DIRECTORY_PATH);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	/**
	 * Called when test run is done
	 *
	 * @param result
	 * @throws Exception
	 */
	@Override
	public void testRunFinished(Result result) throws Exception {
	}

	/**
	 * Called when an atomic test has finished, whether the test succeeds or fails.
	 */
	@Override
	public void testFinished(Description description) {
		String execFileName = description.getTestClass().getPackage().getName() + "-"
				+ description.getTestClass().getSimpleName() + "-"
				+ description.getMethodName() + ".exec";
		jacoco.onTestFinish(execFileName);
	}
}
