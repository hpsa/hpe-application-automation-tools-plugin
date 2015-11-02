// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.clt.tests;

import java.io.Serializable;

final public class TestResult implements Serializable {

    private final String packageName;
    private final String className;
    private final String testName;
    private final TestResultStatus result;
    private final long duration;
    private final long started;

    public TestResult(String packageName, String className, String testName, TestResultStatus result, long duration, long started) {
        this.packageName = packageName;
        this.className = className;
        this.testName = testName;
        this.result = result;
        this.duration = duration;
        this.started = started;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getTestName() {
        return testName;
    }

    public TestResultStatus getResult() {
        return result;
    }

    public long getDuration() {
        return duration;
    }

    public long getStarted() {
        return started;
    }
}
