package com.hp.octane.plugins.jenkins.actions;

/**
 * Created by kashbi on 22/09/2016.
 */
public class UFTParameter {
    private String argName;
    private int argDirection;
    private String argDefaultValue;
    private String argType;
    private int argIsExternal;

    public String getArgName() {
        return argName;
    }

    public void setArgName(String argName) {
        this.argName = argName;
    }

    public int getArgDirection() {
        return argDirection;
    }

    public void setArgDirection(int argDirection) {
        this.argDirection = argDirection;
    }

    public String getArgDefaultValue() {
        return argDefaultValue;
    }

    public void setArgDefaultValue(String argDefaultValue) {
        this.argDefaultValue = argDefaultValue;
    }

    public String getArgType() {
        return argType;
    }

    public void setArgType(String argType) {
        this.argType = argType;
    }

    public int getArgIsExternal() {
        return argIsExternal;
    }

    public void setArgIsExternal(int argIsExternal) {
        this.argIsExternal = argIsExternal;
    }
}
