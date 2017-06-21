package com.hpe.application.automation.tools.results;

import hudson.model.DirectoryBrowserSupport;
import hudson.model.ModelObject;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

public class DetailReport implements ModelObject {

    private String name = "";
    private String color = "";
    private String duration = "";
    private String pass = "";
    private String fail = "";
    private Run<?, ?> build = null;
    private DirectoryBrowserSupport _directoryBrowserSupport = null;

    public DetailReport(Run<?,?> build, String name, DirectoryBrowserSupport directoryBrowserSupport) {
        this.build = build;
        this.name = name;
        _directoryBrowserSupport = directoryBrowserSupport;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @SuppressWarnings("squid:S1452")
    public Run<?, ?> getBuild() {
        return build;
    }

    public String getName() {
        return name;
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {

        if (_directoryBrowserSupport != null)
            _directoryBrowserSupport.generateResponse(req, rsp, this);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String value) {
        color = value;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String value) {
        duration = value;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String value) {
        pass = value;
    }

    public String getFail() {
        return fail;
    }

    public void setFail(String value) {
        fail = value;
    }

}
