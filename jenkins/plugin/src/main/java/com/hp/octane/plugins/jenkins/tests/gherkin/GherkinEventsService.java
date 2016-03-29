package com.hp.octane.plugins.jenkins.tests.gherkin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.apache.logging.log4j.LogManager;

import java.io.File;

/**
 * Created by franksha on 22/03/2016.
 */
public class GherkinEventsService {

  private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(GherkinEventsService.class);

  public static void copyGherkinTestResultsToBuildDir(AbstractBuild build) {
    FilePath workspacePath = build.getWorkspace();

    try {
      FilePath[] gherkinResults = workspacePath.list(GherkinTestResultsCollector.GHERKIN_NGA_RESULTS_XML_);
      String buildDirPath = build.getRootDir().getAbsolutePath();
      for (int i=0; i<gherkinResults.length; i++) {
        String fileName = buildDirPath + File.separator + GherkinTestResultsCollector.GHERKIN_NGA_RESULTS + i + ".xml";
        FilePath gherkinResPathInBuildDir = new FilePath(new File(fileName));
        gherkinResults[i].copyTo(gherkinResPathInBuildDir);
      }
    } catch (Exception e) {
      logger.error("Error getting the Gherkin results file.", e);
    }
  }
}
