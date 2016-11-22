package com.hp.octane.plugins.jenkins.tests.gherkin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by franksha on 22/03/2016.
 */
public class GherkinEventsService {

  private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(GherkinEventsService.class);

  public static void copyGherkinTestResultsToBuildDir(AbstractBuild build) {
    FilePath workspacePath = build.getWorkspace();

    try {
      List<FilePath> gherkinResults = findFile(GherkinTestResultsCollector.GHERKIN_NGA_RESULTS_XML_, workspacePath);
      String buildDirPath = build.getRootDir().getAbsolutePath();
      for (int i=0; i<gherkinResults.size(); i++) {
        String fileName = buildDirPath + File.separator + GherkinTestResultsCollector.GHERKIN_NGA_RESULTS + i + ".xml";
        FilePath gherkinResPathInBuildDir = new FilePath(new File(fileName));
        gherkinResults.get(i).copyTo(gherkinResPathInBuildDir);
      }
    } catch (Exception e) {
      logger.error("Error getting the Gherkin results file.", e);
    }
  }

  private static List<FilePath> findFile(String fileName, FilePath directoy) throws IOException, InterruptedException {
    List<FilePath> results = new ArrayList<>();
    FilePath[] files = directoy.list(fileName);
    for(int i=0; i<files.length; i++) {
      results.add(files[i]);
    }

    List<FilePath> subDirectories = directoy.listDirectories();
    for(FilePath subDirectory : subDirectories) {
      results.addAll(findFile(fileName, subDirectory));
    }

    return results;
  }
}
