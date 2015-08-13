package com.hp.application.automation.tools.results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.DirectoryBrowserSupport;

public class PerformanceReportAction implements Action {

    private static final String PERFORMANCE_REPORT_FOLDER = "PerformanceReport";
    private static final String REPORT_INDEX = "report.index";

    private Map<String, DetailReport> detailReportMap = new LinkedHashMap<String, DetailReport>();

    private AbstractBuild<?,?> build;

    public PerformanceReportAction(AbstractBuild<?,?> build) {
        this.build = build;
        File reportFolder = new File(build.getArtifactsDir().getParent(), PERFORMANCE_REPORT_FOLDER);
        if (reportFolder.exists()) {
            File indexFile = new File(reportFolder, REPORT_INDEX);
            if (indexFile.exists()) {
                File file = new File(build.getArtifactsDir().getParent(), PERFORMANCE_REPORT_FOLDER);
                DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(file), "report", "graph.gif", false);

                try {
                    BufferedReader br = new BufferedReader(new FileReader(indexFile));
                    String line = "";
                    boolean rolling = true;
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split("\t");
                        if (values.length < 1)
                            continue;
                        DetailReport report = new DetailReport(build, values[0], dbs);
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
                        detailReportMap.put(values[0], report);
                    }
                    br.close();
                } catch (IOException e) {
                }
                
            }
        }
    }

    @Override
    public String getIconFileName() {
        return "/plugin/hp-application-automation-tools-plugin/PerformanceReport/LoadRunner.png";
    }

    @Override
    public String getDisplayName() {
        return "Performance Report";
    }

    @Override
    public String getUrlName() {
        return "PerformanceReport";
    }

    public AbstractBuild<?,?> getBuild() {
        return build;
    }

    public Map<String, DetailReport> getDetailReportMap() {
        return detailReportMap;
    }

    public Object getDynamic(String name, StaplerRequest req, StaplerResponse rsp) {
        if (detailReportMap.containsKey(name))
            return detailReportMap.get(name);
        return null;
    }

}
