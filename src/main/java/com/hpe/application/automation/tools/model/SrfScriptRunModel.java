package com.hpe.application.automation.tools.model;

import net.sf.json.JSONObject;

public class SrfScriptRunModel {

    public String id;
    public String name;
    public String start;
    public String yac;
    public JSONObject parent;
    public int durationMs;
    public SrfStatus status;
    public Environment environment;

    public SrfScriptRunModel(String id, String name, int durationMs, SrfStatus status, JSONObject environment, String yac, JSONObject parent){
        this.id = id;
        this.name = name;
        this.durationMs = durationMs;
        this.yac = yac;
        this.parent = parent;
        this.status = status;
        JSONObject os = environment.getJSONObject("os");
        JSONObject browser = environment.getJSONObject("browser");
        this.environment = new Environment(os.getString("name"), os.getString("version"), browser.getString("name"), browser.getString("version"));
    }

    public int getDuration() {
        return durationMs / 1000;
    }

    public String getFullName() {
        return String.format("%s.%s", this.parent.getString("name"), this.name);
    }

    public String getLinkName() {
        String normalizedScriptRunName = this.name.replace("-","_");
        return String.format("%s_%s", normalizedScriptRunName, this.yac);
    }

    public class NameVersion {
        public NameVersion(String name, String version){
            this.name = name;
            this.version = version;
        }
        public String name;
        public String version;
    }

    public class Environment {
        public Environment(NameVersion os, NameVersion browser){
            this.browser = browser;
            this.os = os;
        }
        public Environment(String osName, String osVersion, String browserName, String browserVersion){
            this.browser = new NameVersion(browserName, browserVersion);
            this.os = new NameVersion(osName, osVersion);
        }
        public NameVersion os;
        public NameVersion browser;
        public String resolution;
        public NameVersion platform;
        public String deviceName;
        public String deviceType;
        public String envType;

        @Override
        public String toString() {
            return String.format("%1s %1s %1s %1s", this.os.name, this.os.version, this.browser.name, this.browser.version);
        }
    }

    public enum SrfStatus {
        success("Success"),
        errored("Error"),
        failed("Failed"),
        discarded("Discarded"),
        cancelled("Cancelled");

        private final String text;
        private SrfStatus(final String text) {
            this.text = text;
        }
        @Override
        public String toString() {
            return text;
        }
    }

}
