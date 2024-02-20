/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.tests.detection;

import com.google.inject.Inject;
import com.microfocus.application.automation.tools.octane.OctanePluginTestBase;
import com.microfocus.application.automation.tools.octane.tests.CopyResourceSCM;
import com.microfocus.application.automation.tools.octane.tests.TestUtils;
import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.test.AbstractTestResultAction;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.ToolInstallations;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"squid:S2698","squid:S2699","squid:S3658"})
public class TestNGExtensionTest extends OctanePluginTestBase {

	@ClassRule
	public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Inject
	public TestNGExtension extension = new TestNGExtension();

	private String mavenName;

	@Before
	public void setUp() throws Exception {
		mavenName = ToolInstallations.configureMaven35().getName();
	}

	@Ignore("temp ignore")
	@Test
	public void testFreestyleProject() throws Exception {
		String projectName = "testNG-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);
		project.setScm(new CopyResourceSCM("/helloWorldTestNGRoot"));
		project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" test -Dmaven.repo.local=%s\\m2-temp",
				TestUtils.getMavenHome(),System.getenv("TEMP")), mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("helloWorld/target/surefire-reports/TEST*.xml, helloWorld2/target/surefire-reports/TEST*.xml"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		ResultFields fields = readResultFields(build);
		assertTestNGFields(fields);
	}

	@Test
	public void testFreestyleProjectOneModule() throws Exception {
		String projectName = "testNG-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);
		project.setScm(new CopyResourceSCM("/helloWorldTestNGRoot/helloWorld"));
		project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" test -Dmaven.repo.local=%s\\m2-temp",
				TestUtils.getMavenHome(),System.getenv("TEMP")), mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("target/surefire-reports/TEST*.xml"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		ResultFields fields = readResultFields(build);
		assertTestNGFields(fields);
	}

	@Ignore("temp ignore")
	@Test
	public void testFreestyleProjectCustomLocation() throws Exception {
		String projectName = "testNG-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);
		project.setScm(new CopyResourceSCM("/helloWorldTestNGRoot"));
		project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" test -P custom-report-location -Dmaven.repo.local=%s\\m2-temp",
				TestUtils.getMavenHome(),System.getenv("TEMP")), mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
		project.getPublishersList().add(new JUnitResultArchiver("**\\custom-report-location/**.xml"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		ResultFields fields = readResultFields(build);
		assertTestNGFields(fields);
	}

	@Ignore("temp ignore")
	@Test
	public void testMavenOneModule() throws Exception {
		String projectName = "testNG-job-maven-" + UUID.randomUUID().toString();
		MavenModuleSet mavenProject = rule.createProject(MavenModuleSet.class, projectName);
		mavenProject.runHeadless();
		mavenProject.setMaven(mavenName);
		//mavenProject.setGoals("test -Dmaven.test.failure.ignore=true");
		mavenProject.setGoals(String.format("test --settings \"%s\\conf\\settings.xml\" -Dmaven.repo.local=\"%s\\m2-temp\" -Dmaven.test.failure.ignore=true",TestUtils.getMavenHome(),System.getenv("TEMP")));
		mavenProject.setScm(new CopyResourceSCM("/helloWorldTestNGRoot/helloWorld"));
		AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

		ResultFields fields = readResultFields(build);
		assertTestNGFields(fields);
	}

	@Test
	public void testMavenMultimodule() throws Exception {
		String projectName = "testNG-job-maven-" + UUID.randomUUID().toString();
		System.setProperty("jenkins.security.s2m.RunningBuildFilePathFilter.SKIP","true");
		MavenModuleSet mavenProject = rule.createProject(MavenModuleSet.class, projectName);
		mavenProject.runHeadless();
		mavenProject.setMaven(mavenName);
		//mavenProject.setGoals("test -Dmaven.test.failure.ignore=true");
		mavenProject.setGoals(String.format("test --settings \"%s\\conf\\settings.xml\" -Dmaven.repo.local=\"%s\\m2-temp\" -Dmaven.test.failure.ignore=true",
				TestUtils.getMavenHome(),System.getenv("TEMP")));
		mavenProject.setScm(new CopyResourceSCM("/helloWorldTestNGRoot"));
		MavenModuleSetBuild build = (MavenModuleSetBuild) TestUtils.runAndCheckBuild(mavenProject);

		ResultFields fields = readResultFields(build);
		assertTestNGFields(fields);

		//test detection in all sub-modules that include tests
		Map<MavenModule, MavenBuild> moduleLastBuilds = build.getModuleLastBuilds();
		for (MavenBuild mavenBuild : moduleLastBuilds.values()) {
			AbstractTestResultAction action = mavenBuild.getAction(AbstractTestResultAction.class);
			if (action != null) {
				fields = readResultFields(mavenBuild);
				assertTestNGFields(fields);
			}
		}
	}

	@Test
	public void testMavenOneModuleCustomLocation() throws Exception {
		String projectName = "testNG-job-maven-" + UUID.randomUUID().toString();
		MavenModuleSet mavenProject = rule.createProject(MavenModuleSet.class, projectName);
		mavenProject.runHeadless();
		mavenProject.setMaven(mavenName);
		//mavenProject.setGoals("test -P custom-report-location -Dmaven.test.failure.ignore=true");
		mavenProject.setGoals(String.format("test -P custom-report-location --settings \"%s\\conf\\settings.xml\" -Dmaven.repo.local=\"%s\\m2-temp\" -Dmaven.test.failure.ignore=true",
				TestUtils.getMavenHome(),System.getenv("TEMP")));

		mavenProject.setScm(new CopyResourceSCM("/helloWorldTestNGRoot/helloWorld"));
		AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

		ResultFields fields = readResultFields(build);
		//we do not support combination of custom test report location and not publishing tests
		Assert.assertNull(fields.getFramework());
		Assert.assertNull(fields.getTestLevel());
		Assert.assertNull(fields.getTestingTool());
	}

	@Ignore("temp ignore")
	@Test
	public void testMavenMultimoduleCustomLocation() throws Exception {
		String projectName = "testNG-job-maven-" + UUID.randomUUID().toString();
		MavenModuleSet mavenProject = rule.createProject(MavenModuleSet.class, projectName);
		mavenProject.runHeadless();
		mavenProject.setMaven(mavenName);
		//mavenProject.setGoals("test -P custom-report-location -Dmaven.test.failure.ignore=true");
		mavenProject.setGoals(String.format("test -P custom-report-location --settings \"%s\\conf\\settings.xml\" -Dmaven.repo.local=\"%s\\m2-temp\" -Dmaven.test.failure.ignore=true",
				TestUtils.getMavenHome(),System.getenv("TEMP")));
		mavenProject.setScm(new CopyResourceSCM("/helloWorldTestNGRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

		ResultFields fields = readResultFields(build);
		//we do not support combination of custom test report location and not publishing tests
		Assert.assertNull(fields.getFramework());
		Assert.assertNull(fields.getTestLevel());
		Assert.assertNull(fields.getTestingTool());
	}

	@Ignore("temp ignore")
	@Test
	public void testMavenMultimoduleCustomLocationPublished() throws Exception {
		String projectName = "testNG-job-maven-" + UUID.randomUUID().toString();
		MavenModuleSet mavenProject = rule.createProject(MavenModuleSet.class, projectName);
		mavenProject.runHeadless();
		mavenProject.setMaven(mavenName);
		//mavenProject.setGoals("test -P custom-report-location -Dmaven.test.failure.ignore=true");
		mavenProject.setGoals(String.format("test -P custom-report-location --settings \"%s\\conf\\settings.xml\" -Dmaven.repo.local=\"%s\\m2-temp\" -Dmaven.test.failure.ignore=true",
				TestUtils.getMavenHome(),System.getenv("TEMP")));
		mavenProject.getPublishersList().add(new JUnitResultArchiver("**/custom-report-location/**.xml"));
		mavenProject.setScm(new CopyResourceSCM("/helloWorldTestNGRoot"));
		AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

		ResultFields fields = readResultFields(build);
		assertTestNGFields(fields);
	}

	@Test
	public void testMavenFailsafe() throws Exception {
		String projectName = "testNG-job-maven-failsafe-" + UUID.randomUUID().toString();
		MavenModuleSet mavenProject = rule.createProject(MavenModuleSet.class, projectName);
		mavenProject.runHeadless();
		mavenProject.setMaven(mavenName);
		//mavenProject.setGoals("verify");
		mavenProject.setGoals(String.format("verify --settings \"%s\\conf\\settings.xml\" -Dmaven.repo.local=\"%s\\m2-temp\" -Dmaven.test.failure.ignore=true",
				TestUtils.getMavenHome(),System.getenv("TEMP")));
		mavenProject.setScm(new CopyResourceSCM("/helloWorldFailsafe"));
		AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

		ResultFields fields = readResultFields(build);
		assertTestNGFields(fields);
	}

	@Test
	public void testFindingFiles() throws IOException, InterruptedException {
		temporaryFolder.newFolder("src");
		temporaryFolder.newFolder("target");
		temporaryFolder.newFolder("target", "foo");
		temporaryFolder.newFolder("target", "bar");
		temporaryFolder.newFolder("target", "baz");
		temporaryFolder.newFolder("target", "baz", "qux");

		String[] pathsToXmls = new String[]{
				"target/foo/TEST1.xml",
				"target/foo/TEST2.xml",
				"target/foo/TEST3.xml",
				"target/bar/TEST4.xml",
				"target/bar/TEST5.xml",
				"target/baz/TEST1.xml",
				"target/baz/TEST2.xml",
				"target/baz/qux/TEST1.xml",
				"target/baz/qux/TEST2.xml",
		};

		for (String filePath : pathsToXmls) {
			temporaryFolder.newFile(filePath);
		}

		TestNGExtension.TestNgResultsFileFinder testNgFinder = new TestNGExtension.TestNgResultsFileFinder(null);
		boolean found = testNgFinder.findTestNgResultsFile(temporaryFolder.getRoot(), pathsToXmls);
		Assert.assertFalse(found);

		temporaryFolder.newFile("target/baz/qux/testng-results.xml");
		found = testNgFinder.findTestNgResultsFile(temporaryFolder.getRoot(), pathsToXmls);
		Assert.assertTrue(found);
	}

	@Test
	public void testFindingFilesMavenBuild() throws Exception {
		//running Junit tests - there will be no testng results file
		String projectName = "testNG-job-maven-" + UUID.randomUUID().toString();
		MavenModuleSet mavenProject = rule.createProject(MavenModuleSet.class, projectName);
		mavenProject.runHeadless();
		mavenProject.setMaven(mavenName);

		//mavenProject.setGoals("test -Dmaven.test.failure.ignore=true");
		mavenProject.setGoals(String.format("test --settings \"%s\\conf\\settings.xml\" -Dmaven.repo.local=\"%s\\m2-temp\" -Dmaven.test.failure.ignore=true",TestUtils.getMavenHome(),System.getenv("TEMP")));
		mavenProject.setScm(new CopyResourceSCM("/helloWorldRoot/helloWorld"));
		MavenModuleSetBuild build = (MavenModuleSetBuild) TestUtils.runAndCheckBuild(mavenProject);

		//test detection in all sub-modules that include tests
		Map<MavenModule, MavenBuild> moduleLastBuilds = build.getModuleLastBuilds();
		for (MavenBuild mavenBuild : moduleLastBuilds.values()) {
			AbstractTestResultAction action = mavenBuild.getAction(AbstractTestResultAction.class);
			if (action != null) {
				boolean found = extension.findTestNgResultsFile(mavenBuild);
				Assert.assertFalse(found);

				//creating tesng result file manually
				FilePath folder = mavenBuild.getWorkspace().child("target/surefire-reports/testng-results.xml");
				File report = new File(folder.toURI());
				report.createNewFile();
				found = extension.findTestNgResultsFile(mavenBuild);
				Assert.assertTrue(found);
				report.delete();
			}
		}
	}

	@Test
	public void testFindingFilesMavenBuildFailsafe() throws Exception {
		//running Junit tests - there will be no testng results file
		String projectName = "testNG-job-maven-" + UUID.randomUUID().toString();
		MavenModuleSet mavenProject = rule.createProject(MavenModuleSet.class, projectName);
		mavenProject.runHeadless();
		mavenProject.setMaven(mavenName);
		//mavenProject.setGoals("test -Dmaven.test.failure.ignore=true");
		mavenProject.setGoals(String.format("test --settings \"%s\\conf\\settings.xml\" -Dmaven.repo.local=\"%s\\m2-temp\" -Dmaven.test.failure.ignore=true",TestUtils.getMavenHome(),System.getenv("TEMP")));
		mavenProject.setScm(new CopyResourceSCM("/helloWorldRoot/helloWorld"));
		MavenModuleSetBuild build = (MavenModuleSetBuild) TestUtils.runAndCheckBuild(mavenProject);

		//test detection in all sub-modules that include tests
		Map<MavenModule, MavenBuild> moduleLastBuilds = build.getModuleLastBuilds();
		for (MavenBuild mavenBuild : moduleLastBuilds.values()) {
			AbstractTestResultAction action = mavenBuild.getAction(AbstractTestResultAction.class);
			if (action != null) {
				boolean found = extension.findTestNgResultsFile(mavenBuild);
				Assert.assertFalse(found);

				//creating tesng result file manually
				FilePath reports = mavenBuild.getWorkspace().child("target/failsafe-reports");
				File reportFolder = new File(reports.toURI());
				reportFolder.mkdirs();

				File reportFile = new File(reports.child("testng-results.xml").toURI());
				reportFile.createNewFile();

				found = extension.findTestNgResultsFile(mavenBuild);
				Assert.assertTrue(found);
				reportFile.delete();
				reportFolder.delete();
			}
		}
	}

	private void assertTestNGFields(ResultFields fields) {
		Assert.assertNotNull(fields);
		Assert.assertEquals("TestNG", fields.getFramework());
		Assert.assertNull(fields.getTestingTool());
		Assert.assertNull(fields.getTestLevel());
	}

	private ResultFields readResultFields(AbstractBuild build) throws FileNotFoundException, XMLStreamException {
		File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
		ResultFieldsXmlReader xmlReader = new ResultFieldsXmlReader(new FileReader(mqmTestsXml));
		return xmlReader.readXml().getResultFields();
	}
}
