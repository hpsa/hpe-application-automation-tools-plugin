package com.microfocus.application.automation.tools.uft.model;

import com.microfocus.application.automation.tools.EncryptionUtils;
import hudson.model.Node;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;

import static com.microfocus.application.automation.tools.uft.utils.Constants.*;

public class UftRunAsUser {
    private String username;
    private String encodedPwd;
    private Secret pwd;

    public UftRunAsUser(String username, String encodedPwd) {
        if (StringUtils.isBlank(username) ) {
            throw new IllegalArgumentException(String.format("%s is required", UFT_RUN_AS_USER_NAME));
        } else if (StringUtils.isBlank(encodedPwd)) {
            throw new IllegalArgumentException(String.format("%s is required", UFT_RUN_AS_USER_ENCODED_PWD));
        }
        this.username = username;
        this.encodedPwd = encodedPwd;
    }
    public UftRunAsUser(String username, Secret pwd) {
        if (StringUtils.isBlank(username) ) {
            throw new IllegalArgumentException(String.format("%s is required", UFT_RUN_AS_USER_NAME));
        } else if (pwd == null) {
            throw new IllegalArgumentException(String.format("%s is required", UFT_RUN_AS_USER_PWD));
        }
        this.username = username;
        this.pwd = pwd;
    }

    public String getUsername() {
        return username;
    }

    public String getEncodedPassword() {
        return encodedPwd;
    }

    public String getEncodedPasswordAsEncrypted(Node node) throws EncryptionUtils.EncryptionException {
        return EncryptionUtils.encrypt(encodedPwd, node);
    }

    public Secret getPassword() { return pwd; }

    public String getPasswordAsEncrypted(Node node) throws EncryptionUtils.EncryptionException {
        return EncryptionUtils.encrypt(pwd.getPlainText(), node);
    }
}