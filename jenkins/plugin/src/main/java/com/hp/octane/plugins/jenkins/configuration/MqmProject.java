// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

final public class MqmProject {
    private final String location;
    private final String domain;
    private final String project;

    public MqmProject(String location, String domain, String project) {
        this.location = location;
        this.domain = domain;
        this.project = project;
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
}
