// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import org.junit.Assert;

import java.util.HashSet;
import java.util.Set;

public class TestUtils {

    public static Set<String> helloWorldTests = new HashSet<String>();
    static {
        helloWorldTests.add(testSignature("helloWorld", "hello", "HelloWorldTest", "testOne", TestResultStatus.PASSED));
        helloWorldTests.add(testSignature("helloWorld", "hello", "HelloWorldTest", "testTwo", TestResultStatus.FAILED));
        helloWorldTests.add(testSignature("helloWorld", "hello", "HelloWorldTest", "testThree", TestResultStatus.SKIPPED));
    }

    public static AbstractBuild runAndCheckBuild(AbstractProject project) throws Exception {
        AbstractBuild build = (AbstractBuild) project.scheduleBuild2(0).get();
        if (!build.getResult().isBetterOrEqualTo(Result.UNSTABLE)) { // avoid expensive build.getLog() until condition is met
            Assert.fail("Build status: " + build.getResult() + ", log follows:\n" + build.getLog());
        }
        return build;
    }

    public static String testSignature(TestResult testResult) {
        return testSignature(testResult.getModuleName(), testResult.getPackageName(), testResult.getClassName(),
                testResult.getTestName(), testResult.getResult());
    }

    public static String testSignature(String moduleName, String packageName, String className, String testName, TestResultStatus status) {
        return moduleName + "#" + packageName + "#" + className + "#" + testName + "#" + status.toPrettyName() + "#";
    }

    public static void matchTests(TestResultIterable testResultIterable, long started, Set<String>... expectedTests) {
        Set<String> copy = new HashSet<String>();
        for (Set<String> expected: expectedTests) {
            copy.addAll(expected);
        }
        for(TestResult testResult: testResultIterable) {
            String testSignature = TestUtils.testSignature(testResult);
            Assert.assertTrue("Not found: " + testSignature + " in " + copy, copy.remove(testSignature));
            Assert.assertEquals("Start time differs", started, testResult.getStarted());
        }
        Assert.assertTrue("More tests expected: " + copy.toString(), copy.isEmpty());
    }
}
