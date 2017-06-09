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

import com.hpe.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.test.AbstractTestResultAction;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.remoting.Role;
import org.jenkinsci.remoting.RoleChecker;

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


    @Override
    public ResultFields detect(final Run build) throws IOException, InterruptedException {
        if(!(build instanceof AbstractBuild)){
            return new ResultFields(null, null, null);
        }
//        if(build instanceof WorkflowBuildAdapter){
//            FilePath workspace = BuildHandlerUtils.getWorkspace(build);
//            ((TestResultAction)((WorkflowBuildAdapter)build).getAction(TestResultAction.class)).getResult().getSuites();
//            if (BuildHandlerUtils.getWorkspace(build).act(new TestNgPipelineResultsFileFinder(testResultsPattern))) {
//                return new ResultFields(TESTNG, null, null);
//            }
//        }

        final List<Object> publishers = ((AbstractBuild) build).getProject().getPublishersList().toList();
        for (Object publisher : publishers) {
            if ("hudson.tasks.junit.JUnitResultArchiver".equals(publisher.getClass().getName())) {
                JUnitResultArchiver junit = (JUnitResultArchiver) publisher;
                String testResultsPattern = junit.getTestResults();
                if (BuildHandlerUtils.getWorkspace(build).act(new TestNgResultsFileFinder(testResultsPattern))) {
                    return new ResultFields(TESTNG, null, null);
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
        if (action != null && mavenBuild.getWorkspace().act(new TestNgResultsFileMavenFinder())) {
            return true;
        }
        return false;
    }

    public static class TestNgResultsFileFinder implements FilePath.FileCallable<Boolean> {

        private String testResultsPattern;

        public TestNgResultsFileFinder(String testResultsPattern) {
            this.testResultsPattern = testResultsPattern;
        }

        @Override
        public Boolean invoke(File workspace, VirtualChannel virtualChannel) throws IOException, InterruptedException {
            FileSet fs = Util.createFileSet(workspace, testResultsPattern);
            DirectoryScanner ds = fs.getDirectoryScanner();
            String[] includedFiles = ds.getIncludedFiles();
            File baseDir = ds.getBasedir();

            if (includedFiles.length > 0) {
                if (findTestNgResultsFile(baseDir, includedFiles)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void checkRoles(RoleChecker roleChecker) throws SecurityException {
            roleChecker.check(this, Role.UNKNOWN);
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

    public static class TestNgResultsFileMavenFinder implements FilePath.FileCallable<Boolean> {

        @Override
        public Boolean invoke(File workspace, VirtualChannel virtualChannel) throws IOException, InterruptedException {
            for (String locationInWorkspace : supportedReportFileLocations) {
                File reportFile = new File(workspace, locationInWorkspace);
                if (reportFile.exists()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void checkRoles(RoleChecker roleChecker) throws SecurityException {
            roleChecker.check(this, Role.UNKNOWN);
        }
    }
}