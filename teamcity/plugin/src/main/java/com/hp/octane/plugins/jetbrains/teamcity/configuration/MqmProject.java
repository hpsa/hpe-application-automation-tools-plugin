// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jetbrains.teamcity.configuration;

final public class MqmProject {
    private final String location;
    private final String sharedSpace;

    public MqmProject(String location, String sharedSpace) {
        this.location = location;
        this.sharedSpace = sharedSpace;
    }

    public String getLocation() {
        return location;
    }

    public String getSharedSpace() {
        return sharedSpace;
    }
}
