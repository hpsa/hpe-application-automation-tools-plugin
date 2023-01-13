package com.microfocus.application.automation.tools.uft.model;

import org.apache.commons.lang.StringUtils;

import static com.microfocus.application.automation.tools.uft.utils.Constants.*;

public class UftRunAsUser {
    private String username;
    private String encodedPwd;

    public UftRunAsUser(String username, String encodedPwd) {
        if (StringUtils.isBlank(username) ) {
            throw new IllegalArgumentException(String.format("%s is required", UFT_RUN_AS_USER_NAME));
        } else if (StringUtils.isBlank(encodedPwd)) {
            throw new IllegalArgumentException(String.format("%s is required", UFT_RUN_AS_USER_ENCODED_PWD));
        }
        this.username = username;
        this.encodedPwd = encodedPwd;
    }

    public String getUsername() {
        return username;
    }

    public String getEncodedPassword() {
        return encodedPwd;
    }
}