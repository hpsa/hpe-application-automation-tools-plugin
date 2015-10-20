// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.build;

public final class BuildTypeDescriptor {

    private final String buildType;
    private final String subType;

    public BuildTypeDescriptor(String buildType, String subType) {
        this.buildType = buildType;
        this.subType = subType;
    }

    public String getBuildType() {
        return buildType;
    }

    public String getSubType() {
        return subType;
    }
}
