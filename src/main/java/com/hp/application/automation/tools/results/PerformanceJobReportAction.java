package com.hp.application.automation.tools.results;

import com.hp.application.automation.tools.results.projectparser.performance.LrJobResults;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PerformanceJobReportAction implements Action {

    private AbstractBuild<?,?> build;

    public LrJobResults get_resultFiles() {
        return _resultFiles;
    }

    private LrJobResults _resultFiles;

    public PerformanceJobReportAction(AbstractBuild<?,?> build, LrJobResults resultFiles) {
        this.build = build;
        this._resultFiles = resultFiles;
    }


    public void getJsonData()
    {

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


}
