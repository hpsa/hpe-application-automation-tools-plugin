package com.hp.octane.plugins.jenkins.tests;// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

import com.hp.octane.plugins.jenkins.identity.ServerIdentity;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import hudson.tasks.junit.JUnitResultArchiver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TestJUnitResults {

    final private static String projectName = "junit-job";

    @Rule
    final public JenkinsRule rule = new JenkinsRule();

    @BeforeClass
    public static void initClass() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testJUnitResults() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("install", mavenInstallation.getName(), null, null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        String value = FileUtils.readFileToString(new File(build.getRootDir(), "mqmTests.xml"));
        compareXml(expectedXml("/xml/mqmTests.xml"), value);
    }

    @Test
    public void testJUnitResultsPom() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("install", mavenInstallation.getName(), "helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        String value = FileUtils.readFileToString(new File(build.getRootDir(), "mqmTests.xml"));
        compareXml(expectedXml("/xml/mqmTestsPom.xml"), value);
    }

    @Test
    public void testJUnitResultsTwoPoms() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(projectName);
        Maven.MavenInstallation mavenInstallation = rule.configureDefaultMaven();
        project.getBuildersList().add(new Maven("install", mavenInstallation.getName(), "helloWorld/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getBuildersList().add(new Maven("install", mavenInstallation.getName(), "helloWorld2/pom.xml", null, "-Dmaven.test.failure.ignore=true"));
        project.getPublishersList().add(new JUnitResultArchiver("**/target/surefire-reports/*.xml"));
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        String value = FileUtils.readFileToString(new File(build.getRootDir(), "mqmTests.xml"));
        compareXml(expectedXml("/xml/mqmTests.xml"), value);
    }

    private void compareXml(String expected, String value) throws IOException, SAXException {
        Diff myDiff = new Diff(expected, value);
        myDiff.overrideDifferenceListener(new TestResultDifferenceListener());
        Assert.assertTrue(myDiff.toString(), myDiff.identical());
    }

    private String expectedXml(String resourcePath) throws IOException {
        return buildXml(TestJUnitResults.class.getResourceAsStream(resourcePath), ServerIdentity.getIdentity());
    }

    private String buildXml(InputStream template, String uuid) throws IOException {
        String xml = IOUtils.toString(template);
        IOUtils.closeQuietly(template);
        return xml.replaceAll("@@@uuid@@@", uuid);
    }

    private static class TestResultDifferenceListener implements DifferenceListener {

        @Override
        public int differenceFound(Difference difference) {
            if ("duration".equals(difference.getTestNodeDetail().getNode().getNodeName())) {
                return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            } else {
                return RETURN_UPGRADE_DIFFERENCE_NODES_DIFFERENT;
            }
        }
        @Override
        public void skippedComparison(Node node, Node node2) {
        }
    }
}
