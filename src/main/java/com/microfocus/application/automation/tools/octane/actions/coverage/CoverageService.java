/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.actions.coverage;

import hudson.FilePath;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Helper Service for coverage publisher
 */
public class CoverageService {
    private static final String COVERAGE_REPORT_FILE_NAME_PREFIX = "coverage_report";
    private static TaskListener listener;

    public static class Jacoco {
        public static final String JACOCO_TYPE = "JACOCOXML";
        public static final String JACOCO_FILE_EXTENSION = ".xml";
        public static final String JACOCO_DEFAULT_FILE_NAME = "jacoco" + JACOCO_FILE_EXTENSION;
        public static final String JACOCO_DEFAULT_PATH = "**/target/site/*/" + JACOCO_DEFAULT_FILE_NAME;

    }
    public static class Lcov {
        public static final String LCOV_TYPE = "LCOV";
        public static final String LCOV_FILE_EXTENSION = ".info";
        public static final String LCOV_DEFAULT_FILE_NAME = "lcov" + LCOV_FILE_EXTENSION;
        public static final String LCOV_DEFAULT_PATH = "**/coverage/" + LCOV_DEFAULT_FILE_NAME;

    }

    public static String getCoverageReportFileName(int index, String fileSuffix) {
        return COVERAGE_REPORT_FILE_NAME_PREFIX + index + "-" + fileSuffix;
    }

    public static String[] getCoverageFiles(final FilePath workspace, String glob) throws IOException, InterruptedException {
        log(String.format("Looking for files that match the pattern %s in root directory %s", glob, workspace.getName()));
        return workspace.act(new ResultFilesCallable(glob));
    }

    public static void copyCoverageFile(File resultFile, File targetReportFile, final FilePath workspace) throws IOException, InterruptedException {
        log(String.format("Copying %s to %s", resultFile.getPath(), targetReportFile));

        byte[] content = workspace.act(new FileContentCallable(resultFile));

        if (validateContent(content)) {
            log("Got coverage file content");

            try (FileOutputStream os = new FileOutputStream(targetReportFile)) {
                os.write(content);
            }
            log(String.format("coverage file copied successfully to %s", targetReportFile.getPath()));
        } else {
            log("coverage file content corrupted, failed to copy the file to target destination");
        }
    }

    /**
     * most of the validations will be done in octane side
     * this is a place holder to do more validations if needed
     * @param content of the file
     * @return status
     */
    private static boolean validateContent(byte[] content) {
        return content.length > 0;
    }

    public static void log(final String message) {
        if(listener != null) {
            listener.getLogger().println(message);
        }
    }

    public static void setListener(TaskListener l) {
        listener = l;
    }

    /**
     * this class searched for files that match specific pattern
     */
    private static final class ResultFilesCallable extends MasterToSlaveFileCallable<String[]> {
        private final String glob;

        private ResultFilesCallable(String glob) {
            this.glob = glob;
        }

        @Override
        public String[] invoke(File rootDir, VirtualChannel channel) throws IOException {
            FileSet fs = Util.createFileSet(rootDir, glob);
            DirectoryScanner ds = fs.getDirectoryScanner();
            return ds.getIncludedFiles();
        }
    }

    private static final class FileContentCallable extends MasterToSlaveFileCallable<byte[]> {
        private final File file;

        private FileContentCallable(File file) {
            this.file = file;
        }

        @Override
        public byte[] invoke(File rootDir, VirtualChannel channel) throws IOException {
            return Files.readAllBytes(file.toPath());
        }
    }

}
