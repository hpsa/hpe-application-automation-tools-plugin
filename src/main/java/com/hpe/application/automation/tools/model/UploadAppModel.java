package com.hpe.application.automation.tools.model;

import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 5/17/16
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class UploadAppModel {
    private String mcServerName;
    private String mcUserName;
    private Secret mcPassword;
    private ProxySettings proxySettings;
    private List<UploadAppPathModel> applicationPaths;

    @DataBoundConstructor
    public UploadAppModel(String mcServerName, String mcUserName, String mcPassword, ProxySettings proxySettings, List<UploadAppPathModel> applicationPaths) {
        this.mcServerName = mcServerName;
        this.mcUserName = mcUserName;
        this.mcPassword = Secret.fromString(mcPassword);
        this.proxySettings = proxySettings;
        this.applicationPaths = applicationPaths;
    }

    public String getMcServerName() {
        return mcServerName;
    }

    public String getMcUserName() {
        return mcUserName;
    }

    public String getMcPassword() {
        return mcPassword.getPlainText();
    }

    public ProxySettings getProxySettings() {
        return proxySettings;
    }

    public boolean isUseProxy() {
        return proxySettings != null;
    }

    public boolean isUseAuthentication() {
        return proxySettings != null && StringUtils.isNotBlank(proxySettings.getFsProxyUserName());
    }

    public List<UploadAppPathModel> getApplicationPaths() {
        return applicationPaths;
    }
}
