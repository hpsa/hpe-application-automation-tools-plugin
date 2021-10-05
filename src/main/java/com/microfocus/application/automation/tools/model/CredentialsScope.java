package com.microfocus.application.automation.tools.model;

public enum CredentialsScope {
    JOB("Job (local) credentials"),
    SYSTEM("System (global) credentials");

    private final String description;
    CredentialsScope(String description) {
        this.description = description;
    }

    public String getValue(){
        return this.toString();
    }
    public String getDescription(){
        return this.description;
    }
}
