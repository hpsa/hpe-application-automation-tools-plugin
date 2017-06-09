package com.hpe.application.automation.tools.model;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.util.Secret;

/**
 * This model is for sse build step's proxy setting.
 * It's different from the class ProxySettings. Here we use credentials instead of name/password.
 * @author llu4
 *
 */
public class SseProxySettings {
    private String fsProxyAddress;
    private String fsProxyCredentialsId;
    
    /**
     * To store the user name which get from the credentials.
     * Is set in sseBuilder while performing.
     */
    private String fsProxyUserName;
    
    /**
     * To store the password which get from the credentials.
     * Is set in sseBuilder while performing.
     */
    private Secret fsProxyPassword;

    /**
     * These two variables are set directly by the jelly form.
     */
    @DataBoundConstructor
    public SseProxySettings(String fsProxyAddress, String fsProxyCredentialsId) {
        this.fsProxyAddress = fsProxyAddress;
        this.fsProxyCredentialsId = fsProxyCredentialsId;
    }

    public String getFsProxyAddress() {
        return fsProxyAddress;
    }

    public String getFsProxyCredentialsId() {
        return fsProxyCredentialsId;
    }

	public String getFsProxyUserName() {
		return fsProxyUserName;
	}

	public void setFsProxyUserName(String fsProxyUserName) {
		this.fsProxyUserName = fsProxyUserName;
	}

	public Secret getFsProxyPassword() {
		return fsProxyPassword;
	}

	public void setFsProxyPassword(Secret fsProxyPassword) {
		this.fsProxyPassword = fsProxyPassword;
	}

}
