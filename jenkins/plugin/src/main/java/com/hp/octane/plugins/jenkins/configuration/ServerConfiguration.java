// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

public class ServerConfiguration {

    public String location;
    public String username;
    public String password;

    public ServerConfiguration(String location, String username, String password) {
        this.location = location;
        this.username = username;
        this.password = password;
    }
}
