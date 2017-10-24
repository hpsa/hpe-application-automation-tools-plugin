/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests;

import com.hpe.application.automation.tools.octane.ResultQueue;
import com.hpe.application.automation.tools.octane.tests.junit.TestResultStatus;
import hudson.matrix.*;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
@SuppressWarnings({"squid:S2699","squid:S3658","squid:S2259","squid:S1872","squid:S2925","squid:S109","squid:S1607","squid:S2701","squid:S2698"})
public class JUnitResultsTest {

	private static Set<String> helloWorld2Tests = new HashSet<>();

	static {
		helloWorld2Tests.add(TestUtils.testSignature("helloWorld2", "hello", "HelloWorld2Test", "testOnce",
                TestResultStatus.PASSED));
		helloWorld2Tests.add(TestUtils.testSignature("helloWorld2", "hello", "HelloWorld2Test", "testDoce", TestResultStatus.PASSED));
	}

	private static Set<String> subFolderHelloWorldTests = new HashSet<>();

	static {
		subFolderHelloWorldTests.add(TestUtils.testSignature("subFolder/helloWorld", "hello", "HelloWorldTest", "testOne", TestResultStatus.PASSED));
		subFolderHelloWorldTests.add(TestUtils.testSignature("subFolder/helloWorld", "hello", "HelloWorldTest", "testTwo", TestResultStatus.FAILED));
		subFolderHelloWorldTests.add(TestUtils.testSignature("subFolder/helloWorld", "hello", "HelloWorldTest", "testThree", TestResultStatus.SKIPPED));
	}

	@ClassRule
	public static final JenkinsRule rule = new JenkinsRule();
	private static String mavenName;

	private TestQueue queue;

	@BeforeClass
	public static void prepareClass() throws Exception {
		rule.jenkins.setNumExecutors(10);
		Maven.MavenInstallation mavenInstallation = ToolInstallations.configureMaven3();

//		Maven.MavenInstallation mavenInstallation = new Maven.MavenInstallation("default-system-maven", System.getenv("MAVEN_HOME"), JenkinsRule.NO_PROPERTIES);
		mavenName = mavenInstallation.getName();
	}

	@Before
	public void prepareTest() {
		TestUtils.createDummyConfiguration();

		TestListener testListener = ExtensionUtil.getInstance(rule, TestListener.class);
		queue = new TestQueue();
		testListener._setTestResultQueue(queue);


	}

	@Test
	public void testJUnitResults() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);

		project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" clean test -Dmaven.repo.local=%s\\m2-temp",
				System.getenv("MAVEN_HOME"),System.getenv("TEMP")), mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, projectName, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton(projectName + "#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsPom() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);

		project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" clean test -Dmaven.repo.local=%s\\m2-temp",
				System.getenv("MAVEN_HOME"),System.getenv("TEMP")), mavenName, "subFolder/helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot", "subFolder"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, projectName, subFolderHelloWorldTests);
		Assert.assertEquals(Collections.singleton(projectName + "#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsTwoPoms() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);

		project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" clean test -Dmaven.repo.local=%s\\m2-temp",
				System.getenv("MAVEN_HOME"), System.getenv("TEMP")), mavenName, "helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, projectName, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton(projectName + "#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsLegacy() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		MavenModuleSet project = rule.createProject(MavenModuleSet.class, projectName);
		project.runHeadless();

		project.setMaven(mavenName);
		project.setGoals(String.format("clean test --settings %s\\conf\\settings.xml -Dmaven.repo.local=%s\\m2-temp -Dmaven.test.failure.ignore=true",
				System.getenv("MAVEN_HOME"),System.getenv("TEMP")));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, projectName, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton(projectName + "#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsLegacyWithoutJUnitArchiver() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		MavenModuleSet project = rule.createProject(MavenModuleSet.class, projectName);
		project.runHeadless();

		project.setMaven(mavenName);
		project.setGoals(String.format("clean test --settings %s\\conf\\settings.xml -Dmaven.repo.local=%s\\m2-temp -Dmaven.test.failure.ignore=true",
				System.getenv("MAVEN_HOME"),System.getenv("TEMP")));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, projectName, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton(projectName + "#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsLegacySubfolder() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		MavenModuleSet project = rule.createProject(MavenModuleSet.class, projectName);
		project.runHeadless();

		project.setMaven(mavenName);
		project.setRootPOM("subFolder/helloWorld/pom.xml");
		project.setGoals(String.format("clean test --settings %s\\conf\\settings.xml -Dmaven.repo.local=%s\\m2-temp -Dmaven.test.failure.ignore=true",
				System.getenv("MAVEN_HOME"),System.getenv("TEMP")));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot", "subFolder"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, projectName, subFolderHelloWorldTests);
		Assert.assertEquals(Collections.singleton(projectName + "#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsWorkspaceStripping() throws Exception {
		Set<String> uftTests = new HashSet<>();
		uftTests.add(TestUtils.testSignature("", "All-Tests", "<None>", "subfolder" + File.separator + "CalculatorPlusNextGen", TestResultStatus.FAILED));

		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);
		project.getPublishersList().add(new TestCustomJUnitArchiver("UFT_results.xml"));
		project.setScm(new CopyResourceSCM("/UFT"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, projectName, uftTests);
		Assert.assertEquals(Collections.singleton(projectName + "#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsFreeStyleModule() throws Exception {
		// this scenario simulates FreeStyle project with maven executed via shell (by not using Maven builder directly)
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);

		project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" clean test -Dmaven.repo.local=%s\\m2-temp",
				System.getenv("MAVEN_HOME"),System.getenv("TEMP")), mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		matchTests(build, projectName, TestUtils.helloWorldTests, helloWorld2Tests);
		Assert.assertEquals(Collections.singleton(projectName + "#1"), getQueuedItems());
	}

	@Test
	public void testJUnitResultsMatrixProject() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		MatrixProject matrixProject = rule.createProject(MatrixProject.class, projectName);
		matrixProject.setAxes(new AxisList(new Axis("osType", "Linux", "Windows")));

        matrixProject.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" clean test -Dmaven.test.failure.ignore=true -Dmaven.repo.local=%s\\m2-temp -X",
				System.getenv("MAVEN_HOME"),System.getenv("TEMP")), mavenName));

        matrixProject.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		matrixProject.setScm(new CopyResourceSCM("/helloWorldRoot"));
		MatrixBuild build = (MatrixBuild) TestUtils.runAndCheckBuild(matrixProject);

		for (MatrixRun run : build.getExactRuns()) {
			matchTests(run, projectName, TestUtils.helloWorldTests, helloWorld2Tests);
		}
		Assert.assertEquals(new HashSet<>(Arrays.asList(projectName + "/osType=Windows#1", projectName + "/osType=Linux#1")), getQueuedItems());
		Assert.assertFalse(new File(build.getRootDir(), "mqmTests.xml").exists());
	}

	private Set<String> getQueuedItems() {
		Set<String> ret = new HashSet<>();
		ResultQueue.QueueItem item;
		while ((item = queue.peekFirst()) != null) {
			ret.add(item.getProjectName() + "#" + item.getBuildNumber());
			queue.remove();
		}
		return ret;
	}

	private void matchTests(AbstractBuild build, String projectName, Set<String>... expectedTests) throws FileNotFoundException {
		File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
		TestUtils.matchTests(new TestResultIterable(mqmTestsXml), projectName, build.getStartTimeInMillis(), expectedTests);
	}
}
