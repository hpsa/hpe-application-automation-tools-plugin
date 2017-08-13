package com.hpe.application.automation.tools.model;

public enum PostRunAction {

    COLLATE("Collate Results"), 
    COLLATE_AND_ANALYZE("Collate and Analyze"),
    DO_NOTHING("Do Not Collate");

    private String value;

    private PostRunAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
