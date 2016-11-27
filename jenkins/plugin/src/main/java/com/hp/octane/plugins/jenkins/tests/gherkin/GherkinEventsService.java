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
      FilePath gherkinResultsPath = workspacePath.child(GherkinTestResultsCollector.RESULTS_FOLDER);
      if(gherkinResultsPath.exists()) {
        FilePath buildDirPath = new FilePath(build.getRootDir());
        gherkinResultsPath.moveAllChildrenTo(buildDirPath);
      }
    } catch (Exception e) {
      logger.error("Error getting the Gherkin results file.", e);
    }
  }
}
