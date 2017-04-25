package com.hp.application.automation.tools.octane.actions;

/**
 * Created by berkovir on 20/04/2017.
 */
public enum UftTestType {
    GUI("gui"), API("api"), None("none");
    private String testType;

    UftTestType(String testType) {
        this.testType = testType;
    }

    public boolean isNone() {
        return this.testType.equals(None.testType);
    }
}
