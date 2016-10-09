package com.hp.application.automation.tools.results;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;
import hudson.tasks.test.TestResultProjectAction;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Generates the transaction summary reports and adds them to the run
 */
public class TransactionSummaryAction implements Action, SimpleBuildStep.LastBuildAction {

    private static final String TRANSACTION_SUMMARY_FOLDER = "TransactionSummary";
    private static final String REPORT_INDEX = "report.index";

    private Map<String, SummaryReport> summaryReportMap = new LinkedHashMap<String, SummaryReport>();
    private final List<TestResultProjectAction> projectActionList;

    private Run<?,?> build;

  /**
   * Instantiates a new Transaction summary action.
   *
   * @param build the build
   */
  public TransactionSummaryAction(Run<?,?> build) {
        this.build = build;
        File reportFolder = new File(build.getArtifactsDir().getParent(), TRANSACTION_SUMMARY_FOLDER);
        if (reportFolder.exists()) {
            File indexFile = new File(reportFolder, REPORT_INDEX);
            if (indexFile.exists()) {
                File file = new File(build.getArtifactsDir().getParent(), TRANSACTION_SUMMARY_FOLDER);
                DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(file), "report", "graph.gif", false);

                try {
                    BufferedReader br = new BufferedReader(new FileReader(indexFile));
                    String line = "";
                    boolean rolling = true;
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split("\t");
                        if (values.length < 1)
                            continue;
                        SummaryReport report = new SummaryReport(build, values[0], dbs);
                        if (rolling) {
                            report.setColor("#FFF");
                            rolling = false;
                        } else {
                            report.setColor("#F1F1F1");
                            rolling = true;
                        }
                        if (values.length >= 2)
                            report.setDuration(values[1]);
                        else
                            report.setDuration("##");
                        if (values.length >= 3)
                            report.setPass(values[2]);
                        else
                            report.setPass("##");
                        if (values.length >= 4)
                            report.setFail(values[3]);
                        else
                            report.setFail("##");
                        summaryReportMap.put(values[0], report);
                    }
                    br.close();
                } catch (IOException e) {
                }
                
            }
        }
        projectActionList = new ArrayList<TestResultProjectAction>();
//        projectActionList.add(new TestResultProjectAction(build.getParent()));
    }

    @Override
    public String getIconFileName() {
        return "/plugin/hp-application-automation-tools-plugin/PerformanceReport/LoadRunner.png";
    }

    @Override
    public String getDisplayName() {
        return "Transaction Summary";
    }

    @Override
    public String getUrlName() {
        return "TransactionSummary";
    }

  /**
   * Gets build.
   *
   * @return the build
   */
  public Run<?,?> getBuild() {
        return build;
    }

  /**
   * Gets summary report map.
   *
   * @return the summary report map
   */
  public Map<String, SummaryReport> getSummaryReportMap() {
        return summaryReportMap;
    }

  /**
   * Gets dynamic.
   *
   * @param name the name 
   * @param req the req 
   * @param rsp the rsp 
   * @return the dynamic
   */
  public Object getDynamic(String name, StaplerRequest req, StaplerResponse rsp) {
        if (summaryReportMap.containsKey(name))
            return summaryReportMap.get(name);
        return null;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return projectActionList;
    }
}
