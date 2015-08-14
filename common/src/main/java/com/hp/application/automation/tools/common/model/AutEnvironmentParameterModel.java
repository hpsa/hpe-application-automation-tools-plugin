package com.hp.application.automation.tools.common.model;

/**
 * Created by mprilepina on 14/08/2015.
 */
public class AutEnvironmentParameterModel {

    private final String name;
    private final String value;
    private final String paramType;
    private final boolean shouldGetOnlyFirstValueFromJson;

    public AutEnvironmentParameterModel(String name, String value, String paramType, boolean shouldGetOnlyFirstValueFromJson) {
        this.name = name;
        this.value = value;
        this.paramType = paramType;
        this.shouldGetOnlyFirstValueFromJson = shouldGetOnlyFirstValueFromJson;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getParamType() {
        return paramType;
    }

    public boolean isShouldGetOnlyFirstValueFromJson() {
        return shouldGetOnlyFirstValueFromJson;
    }
}
