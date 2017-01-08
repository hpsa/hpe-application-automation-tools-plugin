package com.hp.octane.plugins.jenkins.actions.cucumber;

import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import com.hp.octane.plugins.jenkins.Messages;

import java.io.*;
import java.nio.file.Files;

/**
 * Created by franksha on 22/03/2016.
 */
public class CucumberResultsService {

    public static final String GHERKIN_NGA_RESULTS_XML = "OctaneGherkinResults.xml";
    public static final String GHERKIN_NGA_RESULTS = "OctaneGherkinResults";
    public static final String DEFAULT_GLOB = "**/" + GHERKIN_NGA_RESULTS_XML;

    private static BuildListener listener;

    public static String getGherkinResultFileName(int index) {
        return GHERKIN_NGA_RESULTS + index + ".xml";
    }

    public static String[] getCucumberResultFiles(final FilePath workspace, String glob) throws IOException, InterruptedException {
        if (glob.isEmpty() || glob == null) {
            glob = DEFAULT_GLOB;
            log(Messages.CucumberResultsActionEmptyConfiguration(), glob);
        }

        log("Looking for files that match the pattern %s in root directory %s", glob, workspace.getName());
        return workspace.act(new ResultFilesCallable(glob));
    }

    public static void copyResultFile(File resultFile, File destinationFolder, final FilePath workspace) throws IOException, InterruptedException {
        File existingReportFile;
        int existingResultIndex = -1;

        log("Copying %s to %s", resultFile.getPath(), destinationFolder.getPath());

        do {
            existingReportFile = new File(destinationFolder, getGherkinResultFileName(++existingResultIndex));
        } while (existingReportFile.exists());
        log("New file name on destination will be %s", existingReportFile.getPath());

        byte[] content = workspace.act(new FileContentCallable(resultFile));
        log("Got result file content");
        File target = existingReportFile;
        try (FileOutputStream os = new FileOutputStream(target)) {
            os.write(content);
        }
        log("Result file copied to %s", target.getPath());
    }

    public static void log(final String message, final String... stringFormatArgs) {
        if(listener != null) {
            listener.getLogger().println(String.format("%s: %s",
                Messages.CucumberReporterName(), String.format(message, stringFormatArgs)));
        }
    }

    public static void setListener(BuildListener l) {
        listener = l;
    }

    private static final class ResultFilesCallable extends MasterToSlaveFileCallable<String[]> {
        private final String glob;

        private ResultFilesCallable(String glob) {
            this.glob = glob;
        }

        public String[] invoke(File rootDir, VirtualChannel channel) throws IOException {
            FileSet fs = Util.createFileSet(rootDir, glob);
            DirectoryScanner ds = fs.getDirectoryScanner();
            String[] files = ds.getIncludedFiles();
            return files;
        }
    }

    private static final class FileContentCallable extends MasterToSlaveFileCallable<byte[]> {
        private final File file;

        private FileContentCallable(File file) {
            this.file = file;
        }

        public byte[] invoke(File rootDir, VirtualChannel channel) throws IOException {
            return Files.readAllBytes(file.toPath());
        }
    }

}
