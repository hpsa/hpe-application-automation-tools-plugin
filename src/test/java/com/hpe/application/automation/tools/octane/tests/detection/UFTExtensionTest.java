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

package com.hpe.application.automation.tools.octane.tests.detection;

import com.hpe.application.automation.tools.run.RunFromAlmBuilder;
import com.hpe.application.automation.tools.run.RunFromFileBuilder;
import com.hpe.application.automation.tools.octane.tests.TestUtils;
import com.hpe.application.automation.tools.octane.tests.detection.ResultFieldsXmlReader.TestAttributes;
import com.hpe.application.automation.tools.octane.tests.detection.ResultFieldsXmlReader.TestResultContainer;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.scm.SubversionSCM;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("squid:S2699")
public class UFTExtensionTest {

	@ClassRule
	public static final JenkinsRule rule = new JenkinsRule();

	private ResultFieldsDetectionService detectionService;

	@Before
	public void before() {
		detectionService = new ResultFieldsDetectionService();
	}

	@Test
	public void testMockOneBuilder() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);
		project.getBuildersList().add(new RunFromFileBuilder("notExistingTest"));

		AbstractBuild buildMock = Mockito.mock(AbstractBuild.class);
		Mockito.when(buildMock.getProject()).thenReturn(project);

		ResultFields fields = detectionService.getDetectedFields(buildMock);
		assertUFTFields(fields);
	}

	@Test
	public void testMockMoreBuilders() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);
		project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" test -Dmaven.repo.local=%s\\m2-temp",
				System.getenv("MAVEN_HOME"),System.getenv("TEMP")), ToolInstallations.configureMaven3().getName(), null, null, "-Dmaven.test.failure.ignore=true"));
		project.getBuildersList().add(new RunFromAlmBuilder("notExistingServer", "notExistingUser", "password", "domain", "project", "notExistingTests", "", "", "", ""));

		AbstractBuild buildMock = Mockito.mock(AbstractBuild.class);
		Mockito.when(buildMock.getProject()).thenReturn(project);

		ResultFields fields = detectionService.getDetectedFields(buildMock);
		assertUFTFields(fields);
	}

	@Test
	public void testFileBuilder() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);
		project.getBuildersList().add(new RunFromFileBuilder(""));

		//UFT plugin will not find any test -> that will cause failing the scheduled build
		//but as detection runs after completion of run, we are sure, that it did not fail because of detection service
		AbstractBuild build = project.scheduleBuild2(0).get();

		ResultFields fields = detectionService.getDetectedFields(build);
		assertUFTFields(fields);
	}

	@Ignore
	@Test
	public void testUFTEndToEnd() throws Exception {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject project = rule.createFreeStyleProject(projectName);
		//TODO solve storing of example test
		SubversionSCM scm = new SubversionSCM("http://localhost:8083/svn/selenium/branches/uft");
		project.setScm(scm);
		project.getBuildersList().add(new RunFromFileBuilder("Calculator"));
		project.getPublishersList().add(new JUnitResultArchiver("Results*.xml"));
		//this will actually run the UFT test
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
		ResultFieldsXmlReader xmlReader = new ResultFieldsXmlReader(new FileReader(mqmTestsXml));
		TestResultContainer container = xmlReader.readXml();
		assertUFTFields(container.getResultFields());
		assertUFTTestAttributes(container.getTestAttributes());
	}

	private void assertUFTFields(ResultFields fields) {
		Assert.assertNotNull(fields);
		Assert.assertEquals("UFT", fields.getFramework());
		Assert.assertEquals("UFT", fields.getTestingTool());
		Assert.assertNull(fields.getTestLevel());
	}

	private void assertUFTTestAttributes(List<TestAttributes> testAttributes) {
		for (TestAttributes test : testAttributes) {
			Assert.assertTrue(test.getModuleName().isEmpty());
			Assert.assertTrue(test.getPackageName().isEmpty());
			Assert.assertTrue(test.getClassName().isEmpty());
			Assert.assertTrue(!test.getTestName().isEmpty());
		}
	}
}
