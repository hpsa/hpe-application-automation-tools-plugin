package com.microfocus.application.automation.tools.model;

public enum CredentialsScope {
    JOB("Use Job (local) credentials"),
    SYSTEM("Use System (global) credentials");

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
