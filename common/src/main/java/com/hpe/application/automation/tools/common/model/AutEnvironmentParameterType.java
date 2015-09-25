package com.hpe.application.automation.tools.common.model;

/**
 * Created by mprilepina on 14/08/2015.
 */
public enum AutEnvironmentParameterType {
    UNDEFINED(""), ENVIRONMENT("Environment"), EXTERNAL("From JSON"), USER_DEFINED("Manual");

    private String value;

    private AutEnvironmentParameterType(String value) {

        this.value = value;
    }

    public String value() {

        return value;
    }

    public static AutEnvironmentParameterType get(String val) {
        for (AutEnvironmentParameterType parameterType : AutEnvironmentParameterType.values()) {
            if (val.equals(parameterType.value()))
                return parameterType;
        }
        return UNDEFINED;
    }
}
