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

import com.hpe.application.automation.tools.octane.tests.CopyResourceSCM;
import com.hpe.application.automation.tools.octane.tests.ExtensionUtil;
import com.hpe.application.automation.tools.octane.tests.TestUtils;
import com.hpe.application.automation.tools.octane.tests.junit.JUnitExtension;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileReader;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ResultFieldsDetectionTest {

	@Rule
	public final JenkinsRule rule = new JenkinsRule();

	private static FreeStyleProject project;

	private static ResultFieldsDetectionService detectionService;

	private final JenkinsRule.WebClient jClient = rule.createWebClient();

	@Before
	public void setUp() throws Exception {

		project = rule.createFreeStyleProject("junit - job");
		JUnitExtension junitExtension = ExtensionUtil.getInstance(rule, JUnitExtension.class);
		detectionService = Mockito.mock(ResultFieldsDetectionService.class);
		junitExtension._setResultFieldsDetectionService(detectionService);

		Maven.MavenInstallation mavenInstallation = ToolInstallations.configureMaven3();
		//Maven.MavenInstallation mavenInstallation = new Maven.MavenInstallation("default-system-maven", System.getenv("MAVEN_HOME"), JenkinsRule.NO_PROPERTIES);

		//project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" -U test",System.getenv("MAVEN_HOME")), mavenInstallation.getName(), null, null, "-Dmaven.test.failure.ignore=true"));
		project.getBuildersList().add(new Maven(String.format("--settings \"%s\\conf\\settings.xml\" -U test -Dmaven.repo.local=%s\\m2-temp",
				System.getenv("MAVEN_HOME"),System.getenv("TEMP")), mavenInstallation.getName(), null, null, "-Dmaven.test.failure.ignore=true"));

		project.setScm(new CopyResourceSCM("/helloWorldRoot"));

		TestUtils.createDummyConfiguration();
	}

	@Test
	public void testDetectionNotRun() throws Exception {
		//there is no test publisher set up in this project, detection will not run
		AbstractBuild build = TestUtils.runAndCheckBuild(project);
		verify(detectionService, Mockito.never()).getDetectedFields(build);
	}

	@Test
	public void testDetectionRunOnce() throws Exception {
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);
		verify(detectionService, Mockito.times(1)).getDetectedFields(build);
	}

	@Test
	public void testDetectedFieldsInXml() throws Exception {
		when(detectionService.getDetectedFields(any(AbstractBuild.class))).thenReturn(new ResultFields("HOLA", "CIAO", "SALUT"));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
		ResultFieldsXmlReader xmlReader = new ResultFieldsXmlReader(new FileReader(mqmTestsXml));
		ResultFields resultFields = xmlReader.readXml().getResultFields();

		Assert.assertNotNull(resultFields);
		Assert.assertEquals("HOLA", resultFields.getFramework());
		Assert.assertEquals("CIAO", resultFields.getTestingTool());
		Assert.assertEquals("SALUT", resultFields.getTestLevel());
	}

	@Test
	public void testNoDetectedFieldsInXml() throws Exception {
		when(detectionService.getDetectedFields(any(AbstractBuild.class))).thenReturn(null);
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
		ResultFieldsXmlReader xmlReader = new ResultFieldsXmlReader(new FileReader(mqmTestsXml));
		ResultFields resultFields = xmlReader.readXml().getResultFields();
		;

		Assert.assertNull(resultFields.getFramework());
		Assert.assertNull(resultFields.getTestingTool());
		Assert.assertNull(resultFields.getTestLevel());
	}

	@Test
	public void testEmptyDetectedFieldsInXml() throws Exception {
		when(detectionService.getDetectedFields(any(AbstractBuild.class))).thenReturn(new ResultFields(null, null, null));
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
		ResultFieldsXmlReader xmlReader = new ResultFieldsXmlReader(new FileReader(mqmTestsXml));
		ResultFields resultFields = xmlReader.readXml().getResultFields();

		Assert.assertNull(resultFields.getFramework());
		Assert.assertNull(resultFields.getTestingTool());
		Assert.assertNull(resultFields.getTestLevel());
	}

	/**
	 * We do not detect Junit yet.
	 */
	@Test
	public void testNotDetectableConfigurationInXml() throws Exception {
		project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
		AbstractBuild build = TestUtils.runAndCheckBuild(project);

		File mqmTestsXml = new File(build.getRootDir(), "mqmTests.xml");
		ResultFieldsXmlReader xmlReader = new ResultFieldsXmlReader(new FileReader(mqmTestsXml));
		ResultFields resultFields = xmlReader.readXml().getResultFields();

		Assert.assertNull(resultFields.getFramework());
		Assert.assertNull(resultFields.getTestingTool());
		Assert.assertNull(resultFields.getTestLevel());
	}
}
