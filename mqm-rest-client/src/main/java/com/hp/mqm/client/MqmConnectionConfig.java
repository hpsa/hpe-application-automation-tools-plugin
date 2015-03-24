package com.hp.mqm.client;

public class MqmConnectionConfig {

    private String location;
    private String domain;
    private String project;
    private String username;
    private String password;
    private String proxyHost;
    private Integer proxyPort;

    public MqmConnectionConfig() {
    }

    public MqmConnectionConfig(String location, String domain, String project, String username, String password, String proxyHost, Integer proxyPort) {
        this.location = location;
        this.domain = domain;
        this.project = project;
        this.username = username;
        this.password = password;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getLocation() {
        return location;
    }

    public String getDomain() {
        return domain;
    }

    public String getProject() {
        return project;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

}
