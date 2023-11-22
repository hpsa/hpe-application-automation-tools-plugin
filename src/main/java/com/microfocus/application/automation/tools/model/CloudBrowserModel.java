package com.microfocus.application.automation.tools.model;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class CloudBrowserModel implements Serializable {
    private String url;
    private String version;
    private String type;
    private String region;
    private String os;

    @DataBoundConstructor
    public CloudBrowserModel(String cloudBrowserUrl, String cloudBrowserType, String cloudBrowserVersion, String cloudBrowserRegion, String cloudBrowserOs) {
        this.url = cloudBrowserUrl;
        this.type = cloudBrowserType;
        this.version = cloudBrowserVersion;
        this.os = cloudBrowserOs;
        this.region = cloudBrowserRegion;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getRegion() {
        return region;
    }

    public String getOs() {
        return os;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
