package com.hp.application.automation.tools;


import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Range;


public class PerformanceProjectAction implements Action{

    private static final String CSV_RESULT_FOLDER = "CSV";
    private static final String JUNIT_RESULT_NAME = "junitResult.xml";
    public final AbstractProject<?, ?> currentProject;
    private ArrayList<String> buildPerformanceReportList;



    /** Logger. */
    private static final Logger LOGGER = Logger
            .getLogger(PerformanceProjectAction.class.getName());



    public List<String> getBuildPerformanceReportList(){
        this.buildPerformanceReportList = new ArrayList<String>(0);
        if (null == this.currentProject) {
            return this.buildPerformanceReportList;
        }

//        if (null == this.currentProject.getSomeBuildWithWorkspace()) {
//            return buildPerformanceReportList;
//        }

        List<? extends AbstractBuild<?, ?>> builds = currentProject.getBuilds();
        int nbBuildsToAnalyze = builds.size();
//        Range buildsLimits = getFirstAndLastBuild(request, builds);

        for (AbstractBuild<?, ?> currentBuild : builds) {

            buildPerformanceReportList.add(currentBuild.getId());
        }
        return buildPerformanceReportList;
    }

    public PerformanceProjectAction(AbstractProject<?, ?> project) {
            this.currentProject = project;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/hp-application-automation-tools-plugin/PerformanceReport/LoadRunner.png";
    }

    @Override
    public String getDisplayName() {
        return "Project Performance report";
    }

    @Override
    public String getUrlName() {
        return "PerformanceProjectReport";
    }

    @JavaScriptMethod
    public int add(int x, int y) {
        return x+y;
    }
}
