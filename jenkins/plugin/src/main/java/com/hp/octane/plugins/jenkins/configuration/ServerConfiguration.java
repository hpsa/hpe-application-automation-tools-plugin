// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

final public class ServerConfiguration {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerConfiguration that = (ServerConfiguration) o;

        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
