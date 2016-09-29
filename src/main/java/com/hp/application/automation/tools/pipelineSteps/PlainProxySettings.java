package com.hp.application.automation.tools.pipelineSteps;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This class is for getting proxysettings with password haven't been secreted.
 * 
 * @author llu4
 *
 */
public class PlainProxySettings {
	private boolean fsUseAuthentication;
	private String fsProxyAddress;
	private String fsProxyUserName;
	private String fsProxyPassword;

	@DataBoundConstructor
	public PlainProxySettings(boolean fsUseAuthentication, String fsProxyAddress, String fsProxyUserName,
			String fsProxyPassword) {
		this.fsUseAuthentication = fsUseAuthentication;
		this.fsProxyAddress = fsProxyAddress;
		this.fsProxyUserName = fsProxyUserName;
		this.fsProxyPassword = fsProxyPassword;
	}

	public boolean isFsUseAuthentication() {
		return fsUseAuthentication;
	}

	public String getFsProxyAddress() {
		return fsProxyAddress;
	}

	public String getFsProxyUserName() {
		return fsProxyUserName;
	}

	public String getFsProxyPassword() {
		return fsProxyPassword;
	}
}