package com.hp.mqm.clt;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Settings {

    private static final String PROP_SERVER = "server";
    private static final String PROP_DOMAIN = "domain";
    private static final String PROP_PROJECT = "project";
    private static final String PROP_WORKSPACE = "workspace";
    private static final String PROP_USER = "user";
    private static final String PROP_PASSWORD_FILE = "passwordFile";

    private String server;
    private String domain;
    private String project;
    private Integer workspace;

    private String user;
    private String password;
    private String passwordFile;

    private boolean internal = false;
    private String configFile;
    private String outputFile;

    private List<String> tags;
    private List<String> fields;

    private Integer release;
    private Integer productArea;
    private Integer requirement;

    private String buildServer;
    private String buildJob;
    private String buildNumber;
    private String buildStatus;

    private List<String> fileNames;

    public void load(String filename) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = getClass().getResourceAsStream(filename);
        properties.load(inputStream);
        inputStream.close();
        server = properties.getProperty(PROP_SERVER);
        domain = properties.getProperty(PROP_DOMAIN);
        project = properties.getProperty(PROP_PROJECT);
        workspace = properties.getProperty(PROP_WORKSPACE) != null ? Integer.valueOf(properties.getProperty(PROP_WORKSPACE)) : null;
        user = properties.getProperty(PROP_USER);
        passwordFile = properties.getProperty(PROP_PASSWORD_FILE);
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Integer getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Integer workspace) {
        this.workspace = workspace;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordFile() {
        return passwordFile;
    }

    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Integer getRelease() {
        return release;
    }

    public void setRelease(Integer release) {
        this.release = release;
    }

    public Integer getProductArea() {
        return productArea;
    }

    public void setProductArea(Integer productArea) {
        this.productArea = productArea;
    }

    public Integer getRequirement() {
        return requirement;
    }

    public void setRequirement(Integer requirement) {
        this.requirement = requirement;
    }

    public String getBuildServer() {
        return buildServer;
    }

    public void setBuildServer(String buildServer) {
        this.buildServer = buildServer;
    }

    public String getBuildJob() {
        return buildJob;
    }

    public void setBuildJob(String buildJob) {
        this.buildJob = buildJob;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(String buildStatus) {
        this.buildStatus = buildStatus;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }
}
