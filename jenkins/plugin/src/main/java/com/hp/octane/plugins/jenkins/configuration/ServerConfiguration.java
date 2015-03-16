// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

public class ServerConfiguration {

    public String location;
    public String domain;
    public String project;
    public String username;
    public String password;

    public ServerConfiguration(String location, String domain, String project, String username, String password) {
        this.location = location;
        this.domain = domain;
        this.project = project;
        this.username = username;
        this.password = password;
    }
}
