package com.microfocus.application.automation.tools.mc;

public enum AuthType {
    Base,
    Token,
    Unknown;

    public static AuthType fromString(String value) {
        switch(value.toLowerCase()) {
            case "base":
                return AuthType.Base;
            case "token":
                return AuthType.Token;
            default:
                return AuthType.Unknown;
        }
    }
}
