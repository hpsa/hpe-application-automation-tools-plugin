package com.microfocus.application.automation.tools.model;

import com.microfocus.application.automation.tools.mc.AuthType;
import hudson.util.Secret;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class AuthModel implements Serializable {
    private final String mcUserName;
    private final Secret mcPassword;
    private final String mcTenantId;
    private final Secret mcExecToken;
    private final String value;
    private AuthType authType;

    @DataBoundConstructor
    public AuthModel(String mcUserName, String mcPassword, String mcTenantId, String mcExecToken, String value) {
        this.mcUserName = mcUserName;
        this.mcPassword = Secret.fromString(mcPassword);
        this.mcTenantId = mcTenantId;
        this.mcExecToken = Secret.fromString(mcExecToken);
        this.value = value;
        authType = AuthType.fromString(value);
        if (authType == AuthType.UNKNOWN) {
            if (StringUtils.isNotBlank(mcExecToken)) {
                authType = AuthType.TOKEN;
            } else if (StringUtils.isNotBlank(mcUserName) && StringUtils.isNotBlank(mcPassword)) {
                authType = AuthType.BASE;
            }
        }
    }

    public String getMcUserName() {
        return mcUserName;
    }

    public String getMcPassword() {
        if (null != mcPassword) {
            return mcPassword.getPlainText();
        } else {
            return null;
        }
    }

    public String getMcTenantId() {
        return mcTenantId;
    }

    public String getMcExecToken() {
        if (null != mcExecToken) {
            return mcExecToken.getPlainText();
        } else {
            return null;
        }
    }

    public String getValue() {
        return value;
    }

    public String getMcEncryptedExecToken() {
        if (null != mcExecToken) {
            return mcExecToken.getEncryptedValue();
        } else {
            return null;
        }
    }

    public String getMcEncryptedPassword() {
        if (null != mcPassword) {
            return mcPassword.getEncryptedValue();
        } else {
            return null;
        }
    }

    public AuthType getAuthType() {
        return authType;
    }
}