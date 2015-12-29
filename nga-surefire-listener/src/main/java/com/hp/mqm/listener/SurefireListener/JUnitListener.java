package com.hp.mqm.listener.SurefireListener;
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
        //logger.fine("JUnitListener created");
    }

    JUnitListener(JacocoController jacoco) {
        this.jacoco = jacoco;
    }

    /**
     *  Called when an atomic test is about to be started.
     * */
    @Override
    public void testStarted(Description description) {
        //logger.fine("JUnitListener: test started");
        String execFileName = description.getClassName() + "["
                + description.getMethodName() + "]";
        jacoco.onTestStart(execFileName);
    }

    /**
     * Called when test run is started
     * @param description
     * @throws Exception
     */
    @Override
    public void testRunStarted(Description description) throws Exception {
        //logger.fine("JUnitListener: Test Run Started.");
        // create output directory
        File directory = new File(Paths.REPORT_DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }
    /**
     * Called when test run is done
     * @param result
     * @throws Exception
     */
    @Override
    public void testRunFinished(Result result) throws Exception {
        /*logger.fine("JUnitListener: Test Run Finished. Total tests number = "
                + result.getRunCount());*/
    }
    /**
     *  Called when an atomic test has finished, whether the test succeeds or fails.
     * */
    @Override
    public void testFinished(Description description) {
        /*logger.fine("JUnitListener: Test (" + getName(description)
                + ") Finished.");*/
        String execFileName = description.getClassName() + "["
                + description.getMethodName() + "]";
        jacoco.onTestFinish(execFileName); // dump results to test .exec file

    }

    private static String getName(Description description) {
        if (description != null) {
            return description.getClassName() + " " + description.getMethodName();
        } else {
            return "";
        }
    }
}
