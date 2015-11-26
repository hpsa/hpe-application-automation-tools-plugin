// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.hp.octane.plugins.jenkins.ExtensionUtil;
import com.hp.octane.plugins.jenkins.utils.Utils;
import hudson.Launcher;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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

    private TestQueue queue;

    @Before
    public void prepare() {
        TestListener testListener = ExtensionUtil.getInstance(rule, TestListener.class);
        queue = new TestQueue();
        testListener._setTestResultQueue(queue);
    }

    @Test
    public void testJUnitResults() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("test", mavenInstallation.getName(), null, null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
        Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
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
        Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
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
        Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
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
        Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
    }

    @Test
    public void testJUnitResultsLegacyWithoutJUnitArchiver() throws Exception {
        MavenModuleSet project = rule.createMavenProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.setMaven(mavenInstallation.getName());
        project.setGoals("test -Dmaven.test.failure.ignore=true");
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
        Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
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
        Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
    }

    @Test
    public void testJUnitResultsWorkspaceStripping() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        project.getPublishersList().add(new TestCustomJUnitArchiver("UFT_results.xml"));
        project.setScm(new CopyResourceSCM("/UFT"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        matchTests(build, uftTests);
        Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
    }

    @Test
    public void testJUnitResultsFreeStyleModule() throws Exception {
        // this scenario simulates FreeStyle project with maven executed via shell (by not using Maven builder directly)

        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new MyMaven("test", mavenInstallation.getName(), null, null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
        Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
    }

    @Test
    public void testJUnitResultsMatrixProject() throws Exception {
        MatrixProject matrixProject = rule.createMatrixProject(projectName);
        matrixProject.setAxes(new AxisList(new Axis("OS", "Linux", "Windows")));
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        matrixProject.getBuildersList().add(new Maven("test", mavenInstallation.getName(), null, null, "-Dmaven.test.failure.ignore=true"));
        matrixProject.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        matrixProject.setScm(new CopyResourceSCM("/helloWorldRoot"));

        MatrixBuild build = (MatrixBuild) TestUtils.runAndCheckBuild(matrixProject);
        for (MatrixRun run: build.getExactRuns()) {
            matchTests(run, TestUtils.helloWorldTests, helloWorld2Tests);
        }
        Assert.assertEquals(new HashSet<String>(Arrays.asList("junit-job/OS=Windows#1", "junit-job/OS=Linux#1")), getQueuedItems());
        Assert.assertFalse(new File(build.getRootDir(), "mqmTests.xml").exists());
    }

    private Set<String> getQueuedItems() {
        Set<String> ret = new HashSet<String>();
        TestResultQueue.QueueItem item;
        while ((item = queue.peekFirst()) != null) {
            ret.add(item.projectName + "#" + item.buildNumber);
            queue.remove();
        }
        return ret;
    }

    private void matchTests(AbstractBuild build, Set<String> ... expectedTests) throws FileNotFoundException {
        File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
        TestUtils.matchTests(new TestResultIterable(mqmTestsXml), projectName, Utils.timestampInUTC(build.getStartTimeInMillis()), expectedTests);
    }

    private static class MyMaven extends Builder {

        private Maven builder;

        public MyMaven(String targets, String name, String pom, String properties, String jvmOptions) {
            this.builder = new Maven(targets, name, pom, properties, jvmOptions);
        }

        public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            return builder.perform(build, launcher, listener);
        }
    }
}
