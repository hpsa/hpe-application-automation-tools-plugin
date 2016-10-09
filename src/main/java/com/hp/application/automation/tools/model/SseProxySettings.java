package com.hp.application.automation.tools.model;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.util.Secret;

public class SseProxySettings {
    private String fsProxyAddress;
    private String fsProxyCredentialsId;
    
    private String fsProxyUserName;
    private Secret fsProxyPassword;

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
