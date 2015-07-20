package com.hp.octane.plugins.jenkins.tests.detection;

import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.test.AbstractTestResultAction;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Extension(optional = true)
public class TestNGExtension  extends ResultFieldsDetectionExtension {

    private static String TESTNG = "TestNG";

    private static String TESTNG_RESULT_FILE = "testng-results.xml";

    private static final List<String> supportedReportFileLocations = Arrays.asList(
            "target/surefire-reports/" + TESTNG_RESULT_FILE,
            "target/failsafe-reports/" + TESTNG_RESULT_FILE
    );

    /**
     * Basically, the detection is based on finding the <code>testng-results.xml</code> file in the workspace.
     There are two main approaches for finding:
     *  <ul>
     *      <li>FreestyleProject - takes JUnitResultArchiver pattern and tries to find <code>testng-results.xml</code> in the same directories as test reports</li>
     *      <li>MavenProject - tries to find <code>testng-results.xml</code> in default report location for surefire and failsafe maven plugins</li>
     *  </ul>
     *
     * @param build
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public ResultFields detect(final AbstractBuild build) throws IOException, InterruptedException {
        File workspace = new File(build.getWorkspace().toURI());

        final List<Object> publishers = build.getProject().getPublishersList().toList();
        for (Object publisher : publishers) {
            if ("hudson.tasks.junit.JUnitResultArchiver".equals(publisher.getClass().getName())) {
                JUnitResultArchiver junit = (JUnitResultArchiver) publisher;
                String testResultsPattern = junit.getTestResults();

                FileSet fs = Util.createFileSet(workspace, testResultsPattern);
                DirectoryScanner ds = fs.getDirectoryScanner();
                String[] includedFiles = ds.getIncludedFiles();
                File baseDir = ds.getBasedir();

                if (includedFiles.length > 0) {
                    if (findTestNgResultsFile(baseDir, includedFiles)) {
                        return new ResultFields(TESTNG, null, null);
                    }
                }
            }
        }

        if ("hudson.maven.MavenBuild".equals(build.getClass().getName())) {
            MavenBuild mavenBuild = (MavenBuild) build;
            if (findTestNgResultsFile(mavenBuild)) {
                return new ResultFields(TESTNG, null, null);
            }
        }

        if ("hudson.maven.MavenModuleSetBuild".equals(build.getClass().getName())) {
            Map<MavenModule, MavenBuild> moduleLastBuilds = ((MavenModuleSetBuild) build).getModuleLastBuilds();
            for (MavenBuild mavenBuild: moduleLastBuilds.values()) {
                if (findTestNgResultsFile(mavenBuild)) {
                    return new ResultFields(TESTNG, null, null);
                }
            }
        }
        return null;
    }

    boolean findTestNgResultsFile(MavenBuild mavenBuild) throws IOException, InterruptedException {
        AbstractTestResultAction action = mavenBuild.getAction(AbstractTestResultAction.class);
        //try finding only if the maven build includes tests
        if (action != null) {
            for (String locationInWorkspace : supportedReportFileLocations) {
                FilePath reportFile = new FilePath(mavenBuild.getWorkspace(), locationInWorkspace);
                if (reportFile.exists()) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean findTestNgResultsFile(File baseDir, String[] includedFiles) throws IOException, InterruptedException {
        Set<FilePath> directoryCache = new LinkedHashSet<FilePath>();

        for (String path : includedFiles) {
            FilePath file = new FilePath(baseDir).child(path);
            if (file.exists() && !directoryCache.contains(file.getParent())) {
                directoryCache.add(file.getParent());
                FilePath testNgResulsFile = new FilePath(file.getParent(), TESTNG_RESULT_FILE);
                if (testNgResulsFile.exists()) {
                    return true;
                }
            }
        }
        return false;
    }
}