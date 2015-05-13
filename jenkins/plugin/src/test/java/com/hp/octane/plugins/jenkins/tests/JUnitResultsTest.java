// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public class JUnitResultsTest {

    final private static String projectName = "junit-job";

    public static Set<String> helloWorld2Tests = new HashSet<String>();
    static {
        helloWorld2Tests.add(TestUtils.testSignature("helloWorld2", "hello", "HelloWorld2Test", "testOnce", TestResultStatus.PASSED));
        helloWorld2Tests.add(TestUtils.testSignature("helloWorld2", "hello", "HelloWorld2Test", "testDoce", TestResultStatus.PASSED));
    }

    private static Set<String> subFolderHelloWorldTests = new HashSet<String>();
    static {
        subFolderHelloWorldTests.add(TestUtils.testSignature("subFolder/helloWorld", "hello", "HelloWorldTest", "testOne", TestResultStatus.PASSED));
        subFolderHelloWorldTests.add(TestUtils.testSignature("subFolder/helloWorld", "hello", "HelloWorldTest", "testTwo", TestResultStatus.FAILED));
        subFolderHelloWorldTests.add(TestUtils.testSignature("subFolder/helloWorld", "hello", "HelloWorldTest", "testThree", TestResultStatus.SKIPPED));
    }

    private static Set<String> uftTests = new HashSet<String>();
    static {
        uftTests.add(TestUtils.testSignature("", "All-Tests", "<None>", "subfolder/CalculatorPlusNextGen", TestResultStatus.FAILED));
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
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
    }

    @Test
    public void testJUnitResultsPom() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("test", mavenInstallation.getName(), "subFolder/helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot", "subFolder"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

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
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
    }

    @Test
    public void testJUnitResultsLegacy() throws Exception {
        MavenModuleSet project = rule.createMavenProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.setMaven(mavenInstallation.getName());
        project.setGoals("test -Dmaven.test.failure.ignore=true");
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
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
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        matchTests(build, subFolderHelloWorldTests);
    }

    @Test
    public void testJUnitResultsWorkspaceStripping() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        project.getPublishersList().add(new TestCustomJUnitArchiver("UFT_results.xml"));
        project.setScm(new CopyResourceSCM("/UFT"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        matchTests(build, uftTests);
    }

    private void matchTests(AbstractBuild build, Set<String> ... expectedTests) throws FileNotFoundException {
        File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
        TestUtils.matchTests(new TestResultIterable(mqmTestsXml), build.getStartTimeInMillis(), expectedTests);
    }
}
