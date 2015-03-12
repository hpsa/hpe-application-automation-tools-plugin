package com.hp.octane.plugins.jenkins.tests;// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TestJUnitResults {

    final private static String projectName = "junit-job";

    private static Set<String> helloWorldTests = new HashSet<String>();
    static {
        helloWorldTests.add(test("/helloWorld", "hello", "HelloWorldTest", "testOne", TestResultStatus.PASSED));
        helloWorldTests.add(test("/helloWorld", "hello", "HelloWorldTest", "testTwo", TestResultStatus.FAILED));
        helloWorldTests.add(test("/helloWorld", "hello", "HelloWorldTest", "testThree", TestResultStatus.SKIPPED));
    }
    private static Set<String> helloWorld2Tests = new HashSet<String>();
    static {
        helloWorld2Tests.add(test("/helloWorld2", "hello", "HelloWorld2Test", "testOnce", TestResultStatus.PASSED));
        helloWorld2Tests.add(test("/helloWorld2", "hello", "HelloWorld2Test", "testDoce", TestResultStatus.PASSED));
    }

    @Rule
    final public JenkinsRule rule = new JenkinsRule();

    @Test
    public void testJUnitResults() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("install", mavenInstallation.getName(), null, null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        matchTests(new File(build.getRootDir(), "mqmTests.xml"), helloWorldTests, helloWorld2Tests);
    }

    @Test
    public void testJUnitResultsPom() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("install", mavenInstallation.getName(), "helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        matchTests(new File(build.getRootDir(), "mqmTests.xml"), helloWorldTests);
    }

    @Test
    public void testJUnitResultsTwoPoms() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("install", mavenInstallation.getName(), "helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getBuildersList().add(new Maven("install", mavenInstallation.getName(), "helloWorld2/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        matchTests(new File(build.getRootDir(), "mqmTests.xml"), helloWorldTests, helloWorld2Tests);
    }

    @Test
    public void testJUnitResultsLegacy() throws Exception {
        MavenModuleSet project = rule.createMavenProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.setMaven(mavenInstallation.getName());
        project.setGoals("install -Dmaven.test.failure.ignore=true");
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        MavenModuleSetBuild build = project.scheduleBuild2(0).get();

        matchTests(new File(build.getRootDir(), "mqmTests.xml"), helloWorldTests, helloWorld2Tests);
    }

    private void matchTests(File mqmTestsXml, Set<String> ... expectedTests) {
        Set<String> copy = new HashSet<String>();
        for (Set<String> expected: expectedTests) {
            copy.addAll(expected);
        }
        for(TestResult testResult: new TestResultIterable(mqmTestsXml)) {
            Assert.assertTrue(copy.remove(test(testResult)));
        }
        Assert.assertTrue(copy.isEmpty());
    }

    private static String test(TestResult testResult) {
        return test(testResult.getModuleName(), testResult.getPackageName(), testResult.getClassName(),
                testResult.getTestName(), testResult.getResult());
    }

    private static String test(String moduleName, String packageName, String className, String testName, TestResultStatus status) {
        return moduleName + "#" + packageName + "#" + className + "#" + testName + "#" + status.name() + "#";
    }
}
