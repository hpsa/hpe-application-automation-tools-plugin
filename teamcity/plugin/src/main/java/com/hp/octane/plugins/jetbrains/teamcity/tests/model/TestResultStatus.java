// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jetbrains.teamcity.tests.model;

public enum TestResultStatus {

    PASSED("Passed"),
    SKIPPED("Skipped"),
    FAILED("Failed");

    private final String prettyName;

    private TestResultStatus(String prettyName) {
        this.prettyName = prettyName;
    }

    public String toPrettyName() {
        return prettyName;
    }

    public static TestResultStatus fromPrettyName(String prettyName) {
        for (TestResultStatus status : values()) {
            if (status.toPrettyName().equals(prettyName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported TestResultStatus '" + prettyName + "'.");
    }
}
