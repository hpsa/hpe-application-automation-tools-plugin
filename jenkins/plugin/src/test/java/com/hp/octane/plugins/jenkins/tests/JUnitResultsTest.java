package com.hp.octane.plugins.jenkins.tests;

import com.hp.octane.plugins.jenkins.ExtensionUtil;
import hudson.Launcher;
import hudson.matrix.*;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JUnitResultsTest {

	final private static String projectName = "junit-job";

	public static Set<String> helloWorld2Tests = new HashSet<>();

	static {
		helloWorld2Tests.add(TestUtils.testSignature("helloWorld2", "hello", "HelloWorld2Test", "testOnce", TestResultStatus.PASSED));
		helloWorld2Tests.add(TestUtils.testSignature("helloWorld2", "hello", "HelloWorld2Test", "testDoce", TestResultStatus.PASSED));
	}

	private static Set<String> subFolderHelloWorldTests = new HashSet<>();

	static {
		subFolderHelloWorldTests.add(TestUtils.testSignature("subFolder/helloWorld", "hello", "HelloWorldTest", "testOne", TestResultStatus.PASSED));
		subFolderHelloWorldTests.add(TestUtils.testSignature("subFolder/helloWorld", "hello", "HelloWorldTest", "testTwo", TestResultStatus.FAILED));
		subFolderHelloWorldTests.add(TestUtils.testSignature("subFolder/helloWorld", "hello", "HelloWorldTest", "testThree", TestResultStatus.SKIPPED));
	}

	private static Set<String> uftTests = new HashSet<>();

	static {
		uftTests.add(TestUtils.testSignature("", "All-Tests", "<None>", "subfolder/CalculatorPlusNextGen", TestResultStatus.FAILED));
	}

	private static String mavenName;

	@Rule
	public static final JenkinsRule rule = new JenkinsRule();

	private TestQueue queue;

	@BeforeClass
	public static void prepareClass() throws Exception {
		rule.jenkins.setNumExecutors(10);
		Maven.MavenInstallation mavenInstallation = ToolInstallations.configureMaven3();
		mavenName = mavenInstallation.getName();
	}

	@Before
	private void prepareTest() {
		TestListener testListener = ExtensionUtil.getInstance(rule, TestListener.class);
		queue = new TestQueue();
		testListener._setTestResultQueue(queue);
	}

	@Test
	public void testJUnitResults() throws Exception {
		FreeStyleProject project = rule.createFreeStyleProject(projectName);

		project.getBuildersList().add(new Maven("-s settings.xml test", mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsPom() throws Exception {
		FreeStyleProject project = rule.createFreeStyleProject(projectName);

		project.getBuildersList().add(new Maven("-s settings.xml test", mavenName, "subFolder/helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot", "subFolder"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, subFolderHelloWorldTests);
		Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsTwoPoms() throws Exception {
		FreeStyleProject project = rule.createFreeStyleProject(projectName);

		project.getBuildersList().add(new Maven("-s settings.xml test", mavenName, "helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
		project.getBuildersList().add(new Maven("-s settings.xml test", mavenName, "helloWorld2/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsLegacy() throws Exception {
		MavenModuleSet project = rule.createProject(MavenModuleSet.class, projectName);
		project.runHeadless();

		project.setMaven(mavenName);
		project.setGoals("-s settings.xml test -Dmaven.test.failure.ignore=true");
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsLegacyWithoutJUnitArchiver() throws Exception {
		MavenModuleSet project = rule.createProject(MavenModuleSet.class, projectName);
		project.runHeadless();

		project.setMaven(mavenName);
		project.setGoals("-s settings.xml test -Dmaven.test.failure.ignore=true");
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsLegacySubfolder() throws Exception {
		MavenModuleSet project = rule.createProject(MavenModuleSet.class, projectName);
		project.runHeadless();

		project.setMaven(mavenName);
		project.setRootPOM("subFolder/helloWorld/pom.xml");
		project.setGoals("-s settings.xml test -Dmaven.test.failure.ignore=true");
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

		project.getBuildersList().add(new MyMaven("-s settings.xml test", mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton("junit-job#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsMatrixProject() throws Exception {
		MatrixProject matrixProject = rule.createProject(MatrixProject.class, projectName);
		matrixProject.setAxes(new AxisList(new Axis("OS", "Linux", "Windows")));

		matrixProject.getBuildersList().add(new Maven("-s settings.xml test", mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
		matrixProject.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		matrixProject.setScm(new CopyResourceSCM("/helloWorldRoot"));

		MatrixBuild build = (MatrixBuild) TestUtils.runAndCheckBuild(matrixProject);
		for (MatrixRun run : build.getExactRuns()) {
			matchTests(run, TestUtils.helloWorldTests, helloWorld2Tests);
		}
		Assert.assertEquals(new HashSet<>(Arrays.asList("junit-job/OS=Windows#1", "junit-job/OS=Linux#1")), getQueuedItems());
		Assert.assertFalse(new File(build.getRootDir(), "mqmTests.xml").exists());
	}

	private Set<String> getQueuedItems() {
		Set<String> ret = new HashSet<>();
		TestResultQueue.QueueItem item;
		while ((item = queue.peekFirst()) != null) {
			ret.add(item.projectName + "#" + item.buildNumber);
			queue.remove();
		}
		return ret;
	}

	private void matchTests(AbstractBuild build, Set<String>... expectedTests) throws FileNotFoundException {
		File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
		TestUtils.matchTests(new TestResultIterable(mqmTestsXml), projectName, build.getStartTimeInMillis(), expectedTests);
	}

	private static class MyMaven extends Builder {

		private Maven builder;

		public MyMaven(String targets, String name, String pom, String properties, String jvmOptions) {
			this.builder = new Maven(targets, name, pom, properties, jvmOptions);
		}

		public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
			return builder.perform(build, launcher, listener);
		}
	}
}
