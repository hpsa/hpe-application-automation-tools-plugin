package com.hp.octane.plugins.jenkins.tests.detection;

import com.hp.application.automation.tools.run.RunFromAlmBuilder;
import com.hp.application.automation.tools.run.RunFromFileBuilder;
import com.hp.octane.plugins.jenkins.tests.TestUtils;
import com.hp.octane.plugins.jenkins.tests.detection.ResultFieldsXmlReader.TestAttributes;
import com.hp.octane.plugins.jenkins.tests.detection.ResultFieldsXmlReader.TestResultContainer;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.scm.SubversionSCM;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileReader;
import java.util.List;

public class UFTExtensionTest {

    final private static String projectName = "uft-job";

    ResultFieldsDetectionService detectionService;

    @Rule
    final public JenkinsRule rule = new JenkinsRule();


    @Before
    public void before() {
        detectionService = new ResultFieldsDetectionService();
    }

    @Test
    public void testMockOneBuilder() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        project.getBuildersList().add(new RunFromFileBuilder("notExistingTest", "", "", "", ""));

        AbstractBuild buildMock = Mockito.mock(AbstractBuild.class);
        Mockito.when(buildMock.getProject()).thenReturn(project);

        ResultFields fields = detectionService.getDetectedFields(buildMock);
        assertUFTFields(fields);
    }

    @Test
    public void testMockMoreBuilders() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
     //   project.getBuildersList().add(new Ant("clean", "", "", "build.xml", ""));
        project.getBuildersList().add(new Maven("test", "apache-maven", null, null, "-Dmaven.test.failure.ignore=true"));
        project.getBuildersList().add(new RunFromAlmBuilder("notExistingServer", "notExistingUser", "password", "domain", "project", "notExistingTests", "", "", "", ""));

        AbstractBuild buildMock = Mockito.mock(AbstractBuild.class);
        Mockito.when(buildMock.getProject()).thenReturn(project);

        ResultFields fields = detectionService.getDetectedFields(buildMock);
        assertUFTFields(fields);
    }

    @Test
    public void testFileBuilder() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        project.getBuildersList().add(new RunFromFileBuilder("", "", "", "", ""));

        //UFT plugin will not find any test -> that will cause failing the scheduled build
        //but as detection runs after completion of run, we are sure, that it did not fail because of detection service
        AbstractBuild build = (AbstractBuild) project.scheduleBuild2(0).get();

        ResultFields fields = detectionService.getDetectedFields(build);
        assertUFTFields(fields);
    }

    @Ignore
    @Test
    public void testUFTEndToEnd() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        //TODO solve storing of example test
        SubversionSCM scm = new SubversionSCM("http://localhost:8083/svn/selenium/branches/uft");
        project.setScm(scm);
        project.getBuildersList().add(new RunFromFileBuilder("Calculator", "2000", "", "", ""));
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
