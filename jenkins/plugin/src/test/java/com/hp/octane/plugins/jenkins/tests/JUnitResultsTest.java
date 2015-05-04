// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class JUnitResultsTest {

    final private static String projectName = "junit-job";

    private static Set<String> helloWorldTests = new HashSet<String>();
    static {
        helloWorldTests.add(test("helloWorld", "hello", "HelloWorldTest", "testOne", TestResultStatus.PASSED));
        helloWorldTests.add(test("helloWorld", "hello", "HelloWorldTest", "testTwo", TestResultStatus.FAILED));
        helloWorldTests.add(test("helloWorld", "hello", "HelloWorldTest", "testThree", TestResultStatus.SKIPPED));
    }
    private static Set<String> helloWorld2Tests = new HashSet<String>();
    static {
        helloWorld2Tests.add(test("helloWorld2", "hello", "HelloWorld2Test", "testOnce", TestResultStatus.PASSED));
        helloWorld2Tests.add(test("helloWorld2", "hello", "HelloWorld2Test", "testDoce", TestResultStatus.PASSED));
    }
    private static Set<String> subFolderHelloWorldTests = new HashSet<String>();
    static {
        subFolderHelloWorldTests.add(test("subFolder/helloWorld", "hello", "HelloWorldTest", "testOne", TestResultStatus.PASSED));
        subFolderHelloWorldTests.add(test("subFolder/helloWorld", "hello", "HelloWorldTest", "testTwo", TestResultStatus.FAILED));
        subFolderHelloWorldTests.add(test("subFolder/helloWorld", "hello", "HelloWorldTest", "testThree", TestResultStatus.SKIPPED));
    }

    @Rule
    final public JenkinsRule rule = new JenkinsRule();

    @Test
    public void testJUnitResults() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("test", mavenInstallation.getName(), null, null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        AbstractBuild build = runAndCheckBuild(project);

        matchTests(build, helloWorldTests, helloWorld2Tests);
    }

    @Test
    public void testJUnitResultsPom() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("test", mavenInstallation.getName(), "subFolder/helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot", "subFolder"));
        AbstractBuild build = runAndCheckBuild(project);

        matchTests(build, subFolderHelloWorldTests);
    }

    @Test
    public void testJUnitResultsTwoPoms() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("test", mavenInstallation.getName(), "helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getBuildersList().add(new Maven("test", mavenInstallation.getName(), "helloWorld2/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        AbstractBuild build = runAndCheckBuild(project);

        matchTests(build, helloWorldTests, helloWorld2Tests);
    }

    @Test
    public void testJUnitResultsLegacy() throws Exception {
        MavenModuleSet project = rule.createMavenProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.setMaven(mavenInstallation.getName());
        project.setGoals("test -Dmaven.test.failure.ignore=true");
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        AbstractBuild build = runAndCheckBuild(project);

        matchTests(build, helloWorldTests, helloWorld2Tests);
    }

    @Test
    public void testJUnitResultsLegacySubfolder() throws Exception {
        MavenModuleSet project = rule.createMavenProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.setMaven(mavenInstallation.getName());
        project.setRootPOM("subFolder/helloWorld/pom.xml");
        project.setGoals("test -Dmaven.test.failure.ignore=true");
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot", "subFolder"));
        AbstractBuild build = runAndCheckBuild(project);

        matchTests(build, subFolderHelloWorldTests);
    }

    private void matchTests(AbstractBuild build, Set<String> ... expectedTests) {
        File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
        Set<String> copy = new HashSet<String>();
        for (Set<String> expected: expectedTests) {
            copy.addAll(expected);
        }
        for(TestResult testResult: new TestResultIterable(mqmTestsXml)) {
            String testSignature = test(testResult);
            Assert.assertTrue("Not found: " + testSignature + " in " + copy, copy.remove(testSignature));
            Assert.assertEquals("Start time differs", build.getStartTimeInMillis(), testResult.getStarted());
        }
        Assert.assertTrue("More tests expected: " + copy.toString(), copy.isEmpty());
    }

    private AbstractBuild runAndCheckBuild(AbstractProject project) throws Exception {
        AbstractBuild build = (AbstractBuild) project.scheduleBuild2(0).get();
        if (!build.getResult().isBetterOrEqualTo(Result.UNSTABLE)) { // avoid expensive build.getLog() until condition is met
            Assert.fail("Build status: " + build.getResult() + ", log follows:\n" + build.getLog());
        }
        return build;
    }

    private static String test(TestResult testResult) {
        return test(testResult.getModuleName(), testResult.getPackageName(), testResult.getClassName(),
                testResult.getTestName(), testResult.getResult());
    }

    private static String test(String moduleName, String packageName, String className, String testName, TestResultStatus status) {
        return moduleName + "#" + packageName + "#" + className + "#" + testName + "#" + status.toPrettyName() + "#";
    }
}
