package com.hpe.application.automation.tools.model;

import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 3/30/16
 * Time: 1:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProxySettings {
    private boolean fsUseAuthentication;
    private String fsProxyAddress;
    private String fsProxyUserName;
    private Secret fsProxyPassword;

    @DataBoundConstructor
    public ProxySettings(boolean fsUseAuthentication, String fsProxyAddress, String fsProxyUserName, Secret fsProxyPassword) {
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
        return fsProxyPassword.getPlainText();
    }
}
