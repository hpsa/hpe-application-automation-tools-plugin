package com.hp.octane.plugins.jenkins.actions.cucumber;

import hudson.Util;
import hudson.model.BuildListener;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import com.hp.octane.plugins.jenkins.Messages;

import java.io.*;
import java.nio.file.Files;

/**
 * Created by franksha on 22/03/2016.
 */
public class CucumberResultsService {

  public static final String GHERKIN_NGA_RESULTS_XML = "OctaneCucumberResults.xml";
  public static final String GHERKIN_NGA_RESULTS = "OctaneCucumberResults";
  public static final String DEFAULT_GLOB = "**/" + GHERKIN_NGA_RESULTS_XML;

  public static String getGherkinResultFileName(int index) {
    return GHERKIN_NGA_RESULTS + index + ".xml";
  }

  public static String[] getCucumberResultFiles(final File rootDir, String glob, final BuildListener listener) throws IOException, InterruptedException {
      if (glob.isEmpty() || glob == null) {
        glob = DEFAULT_GLOB;
        log(listener, Messages.CucumberResultsActionEmptyConfiguration(), glob);
      }

      FileSet fs = Util.createFileSet(rootDir, glob);
      DirectoryScanner ds = fs.getDirectoryScanner();
      String[] files = ds.getIncludedFiles();
      return files;
  }

  public static void copyResultFile(File resultFile, File destinationFolder) throws IOException {
    File existingReportFile;
    int existingResultIndex = -1;
    do {
      existingReportFile = new File(destinationFolder, getGherkinResultFileName(++existingResultIndex));
    } while (existingReportFile.exists());

    Files.copy(resultFile.toPath(), new File(destinationFolder, getGherkinResultFileName(existingResultIndex)).toPath());
  }

  public static void log(final BuildListener listener, final String message, final String... stringFormatArgs) {
      listener.getLogger().println(String.format("%s: %s",
          Messages.CucumberReporterName(), String.format(message, stringFormatArgs)));
  }

}
