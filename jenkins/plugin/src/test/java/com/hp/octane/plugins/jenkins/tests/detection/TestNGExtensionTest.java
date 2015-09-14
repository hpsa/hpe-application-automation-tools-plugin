package com.hp.octane.plugins.jenkins.tests.detection;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.tests.CopyResourceSCM;
import com.hp.octane.plugins.jenkins.tests.TestUtils;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class TestNGExtensionTest {

    @Rule
    final public JenkinsRule rule = new JenkinsRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Inject
    public TestNGExtension extension = new TestNGExtension();

    private String mavenName;

    @Before
    public void setUp() throws Exception {
        mavenName = rule.configureDefaultMaven().getName();
    }

    @Test
    public void testFreestyleProject() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("testNG - job");
        project.setScm(new CopyResourceSCM("/helloWorldTestNGRoot"));
        project.getBuildersList().add(new Maven("test", mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("helloWorld/target/surefire-reports/TEST*.xml, helloWorld2/target/surefire-reports/TEST*.xml"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        ResultFields fields = readResultFields(build);
        assertTestNGFields(fields);
    }

    @Test
    public void testFreestyleProjectOneModule() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("testNG - job");
        project.setScm(new CopyResourceSCM("/helloWorldTestNGRoot/helloWorld"));
        project.getBuildersList().add(new Maven("test", mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("target/surefire-reports/TEST*.xml"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        ResultFields fields = readResultFields(build);
        assertTestNGFields(fields);
    }

    @Test
    public void testFreestyleProjectCustomLocation() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("testNG - job");
        project.setScm(new CopyResourceSCM("/helloWorldTestNGRoot"));
        project.getBuildersList().add(new Maven("test -P custom-report-location", mavenName, null, null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**\\custom-report-location/**.xml"));
        AbstractBuild build = TestUtils.runAndCheckBuild(project);

        ResultFields fields = readResultFields(build);
        assertTestNGFields(fields);
    }

    @Test
    public void testMavenOneModule() throws Exception {
        MavenModuleSet mavenProject = rule.createMavenProject("testNG - maven job");
        mavenProject.setMaven(mavenName);
        mavenProject.setGoals("test -Dmaven.test.failure.ignore=true");
        mavenProject.setScm(new CopyResourceSCM("/helloWorldTestNGRoot/helloWorld"));
        AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

        ResultFields fields = readResultFields(build);
        assertTestNGFields(fields);
    }

    @Test
    public void testMavenMultimodule() throws Exception {
        MavenModuleSet mavenProject = rule.createMavenProject("testNG - maven job");
        mavenProject.setMaven(mavenName);
        mavenProject.setGoals("test -Dmaven.test.failure.ignore=true");
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
        MavenModuleSet mavenProject = rule.createMavenProject("testNG - maven job");
        mavenProject.setMaven(mavenName);
        mavenProject.setGoals("test -P custom-report-location -Dmaven.test.failure.ignore=true");
        mavenProject.setScm(new CopyResourceSCM("/helloWorldTestNGRoot/helloWorld"));
        AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

        ResultFields fields = readResultFields(build);
        //we do not support combination of custom test report location and not publishing tests
        Assert.assertNull(fields.getFramework());
        Assert.assertNull(fields.getTestLevel());
        Assert.assertNull(fields.getTestingTool());
    }

    @Test
    public void testMavenMultimoduleCustomLocation() throws Exception {
        MavenModuleSet mavenProject = rule.createMavenProject("testNG - maven job");
        mavenProject.setMaven(mavenName);
        mavenProject.setGoals("test -P custom-report-location -Dmaven.test.failure.ignore=true");
        mavenProject.setScm(new CopyResourceSCM("/helloWorldTestNGRoot"));
        AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

        ResultFields fields = readResultFields(build);
        //we do not support combination of custom test report location and not publishing tests
        Assert.assertNull(fields.getFramework());
        Assert.assertNull(fields.getTestLevel());
        Assert.assertNull(fields.getTestingTool());
    }

    @Test
    public void testMavenMultimoduleCustomLocationPublished() throws Exception {
        MavenModuleSet mavenProject = rule.createMavenProject("testNG - maven job");
        mavenProject.setMaven(mavenName);
        mavenProject.setGoals("test -P custom-report-location -Dmaven.test.failure.ignore=true");
        mavenProject.getPublishersList().add(new JUnitResultArchiver("**/custom-report-location/**.xml"));
        mavenProject.setScm(new CopyResourceSCM("/helloWorldTestNGRoot"));
        AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

        ResultFields fields = readResultFields(build);
        assertTestNGFields(fields);
    }

    @Test
    public void testMavenFailsafe() throws Exception {
        MavenModuleSet mavenProject = rule.createMavenProject("testNG - maven failsafe job");
        mavenProject.setMaven(mavenName);
        mavenProject.setGoals("verify");
        mavenProject.setScm(new CopyResourceSCM("/helloWorldFailsafe"));
        AbstractBuild build = TestUtils.runAndCheckBuild(mavenProject);

        ResultFields fields = readResultFields(build);
        assertTestNGFields(fields);
    }

    @Test
    public void testFindingFiles() throws IOException, InterruptedException {
        temporaryFolder.newFolder("src");
        temporaryFolder.newFolder("target");
        temporaryFolder.newFolder("target/foo");
        temporaryFolder.newFolder("target/bar");
        temporaryFolder.newFolder("target/baz");
        temporaryFolder.newFolder("target/baz/qux");

        String[] pathsToXmls = new String[] {
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
        MavenModuleSet mavenProject = rule.createMavenProject("testNG - maven job");
        mavenProject.setMaven(mavenName);
        mavenProject.setGoals("test -Dmaven.test.failure.ignore=true");
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
                File report  = new File(folder.toURI());
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
        MavenModuleSet mavenProject = rule.createMavenProject("testNG - maven job");
        mavenProject.setMaven(mavenName);
        mavenProject.setGoals("test -Dmaven.test.failure.ignore=true");
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
                File reportFolder  = new File(reports.toURI());
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
