package com.microfocus.application.automation.tools.mc;

public enum AuthType {
    BASE,
    TOKEN,
    UNKNOWN;

    public static AuthType fromString(String value) {
        switch(value.toLowerCase()) {
            case "base":
                return AuthType.BASE;
            case "token":
                return AuthType.TOKEN;
            default:
                return AuthType.UNKNOWN;
        }
    }
}
