// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.model;

import com.hp.application.automation.tools.mc.Constants;
import com.hp.application.automation.tools.mc.HttpResponse;
import com.hp.application.automation.tools.mc.HttpUtils;
import com.hp.application.automation.tools.mc.JobConfigurationProxy;
import hudson.EnvVars;
import hudson.util.Secret;
import hudson.util.VariableResolver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class RunFromFileSystemModel {

    private String fsTests;
    private String fsTimeout;
    private String controllerPollingInterval = "30";
    private String perScenarioTimeOut = "10";
    private String ignoreErrorStrings;
    private String mcServerName;
    private String fsUserName;
    private Secret fsPassword;
    private String fsDeviceId;
    private String fsOs;
    private String fsManufacturerAndModel;
    private String fsTargetLab;
    private String fsAutActions;
    private String fsLaunchAppName;
    private String fsInstrumented;
    private String fsDevicesMetrics;
    private String fsExtraApps;
    private String fsJobId;
    private ProxySettings proxySettings;
    private boolean useSSL;

    @DataBoundConstructor
    public RunFromFileSystemModel(String fsTests, String fsTimeout, String controllerPollingInterval,String perScenarioTimeOut, String ignoreErrorStrings, String mcServerName, String fsUserName, String fsPassword, String fsDeviceId, String fsTargetLab, String fsManufacturerAndModel, String fsOs, String fsAutActions, String fsLaunchAppName, String fsDevicesMetrics, String fsInstrumented, String fsExtraApps, String fsJobId, ProxySettings proxySettings, boolean useSSL) {

        this.fsTests = fsTests;

        if (!this.fsTests.contains("\n")) {
            this.fsTests += "\n";
        }

        this.fsTimeout = fsTimeout;


        this.perScenarioTimeOut = perScenarioTimeOut;
        this.controllerPollingInterval = controllerPollingInterval;
        this.ignoreErrorStrings = ignoreErrorStrings;

        this.mcServerName = mcServerName;
        this.fsUserName = fsUserName;
        this.fsPassword = Secret.fromString(fsPassword);
        this.fsDeviceId = fsDeviceId;
        this.fsOs = fsOs;
        this.fsManufacturerAndModel = fsManufacturerAndModel;
        this.fsTargetLab = fsTargetLab;
        this.fsAutActions = fsAutActions;
        this.fsLaunchAppName = fsLaunchAppName;
        this.fsAutActions = fsAutActions;
        this.fsDevicesMetrics = fsDevicesMetrics;
        this.fsInstrumented = fsInstrumented;
        this.fsExtraApps = fsExtraApps;
        this.fsJobId = fsJobId;
        this.proxySettings = proxySettings;
        this.useSSL = useSSL;
    }


    public String getFsTests() {
        return fsTests;
    }

    public String getFsTimeout() {
        return fsTimeout;
    }

    public String getMcServerName() {
        return mcServerName;
    }

    public String getFsUserName() {
        return fsUserName;
    }

    public String getFsPassword() {
        return fsPassword.getPlainText();
    }

    public String getFsDeviceId() {
        return fsDeviceId;
    }

    public String getFsOs() {
        return fsOs;
    }

    public String getFsManufacturerAndModel() {
        return fsManufacturerAndModel;
    }

    public String getFsTargetLab() {
        return fsTargetLab;
    }

    public String getFsAutActions() {
        return fsAutActions;
    }

    public String getFsLaunchAppName() {
        return fsLaunchAppName;
    }

    public String getFsInstrumented() {
        return fsInstrumented;
    }

    public String getFsDevicesMetrics() {
        return fsDevicesMetrics;
    }

    public String getFsExtraApps() {
        return fsExtraApps;
    }

    public String getFsJobId() {
        return fsJobId;
    }

    public boolean isUseProxy() {
        return proxySettings != null;
    }

    public boolean isUseAuthentication() {
        return proxySettings != null && StringUtils.isNotBlank(proxySettings.getFsProxyUserName());
    }

    public ProxySettings getProxySettings() {
        return proxySettings;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    /**
     * @return the controllerPollingInterval
     */
    public String getControllerPollingInterval() {
        return controllerPollingInterval;
    }

    /**
     * @param controllerPollingInterval the controllerPollingInterval to set
     */
    public void setControllerPollingInterval(String controllerPollingInterval) {
        this.controllerPollingInterval = controllerPollingInterval;
    }

    /**
     * @return the ignoreErrorStrings
     */
    public String getIgnoreErrorStrings() {
        return ignoreErrorStrings;
    }


    /**
     * @param ignoreErrorStrings the ignoreErrorStrings to set
     */
    public void setIgnoreErrorStrings(String ignoreErrorStrings) {
        this.ignoreErrorStrings = ignoreErrorStrings;
    }



    /**
     * @return the perScenarioTimeOut
     */
    public String getPerScenarioTimeOut() {
        return perScenarioTimeOut;
    }

    /**
     * @param perScenarioTimeOut the perScenarioTimeOut to set
     */
    public void setPerScenarioTimeOut(String perScenarioTimeOut) {
        this.perScenarioTimeOut = perScenarioTimeOut;
    }

    public Properties getProperties(EnvVars envVars,
                                    VariableResolver<String> varResolver) {
        return CreateProperties(envVars, varResolver);
    }

    public Properties getProperties() {
        return CreateProperties(null, null);
    }

    private Properties CreateProperties(EnvVars envVars,
                                        VariableResolver<String> varResolver) {
        Properties props = new Properties();

        if (!StringUtils.isEmpty(this.fsTests)) {
            String expandedFsTests = envVars.expand(fsTests);
            String[] testsArr = expandedFsTests.replaceAll("\r", "").split("\n");

            int i = 1;

            for (String test : testsArr) {
                props.put("Test" + i, test);
                i++;
            }
        } else {
            props.put("fsTests", "");
        }


        if (StringUtils.isEmpty(fsTimeout)){
            props.put("fsTimeout", "-1");
        }
        else{
            props.put("fsTimeout", "" + fsTimeout);
        }


        if (StringUtils.isEmpty(controllerPollingInterval)){
            props.put("controllerPollingInterval", "30");
        }
        else{
            props.put("controllerPollingInterval", "" + controllerPollingInterval);
        }

        if (StringUtils.isEmpty(perScenarioTimeOut)){
            props.put("PerScenarioTimeOut", "10");
        }
        else{
            props.put("PerScenarioTimeOut", ""+ perScenarioTimeOut);
        }

        if (!StringUtils.isEmpty(ignoreErrorStrings.replaceAll("\\r|\\n", ""))){
            props.put("ignoreErrorStrings", ""+ignoreErrorStrings.replaceAll("\r", ""));
        }

        if (StringUtils.isNotBlank(fsUserName)){
            props.put("MobileUserName", fsUserName);
        }

        if(isUseProxy()){
            props.put("MobileUseProxy", "1");
            props.put("MobileProxyType","2");
            props.put("MobileProxySetting_Address", proxySettings.getFsProxyAddress());
            if(isUseAuthentication()){
                props.put("MobileProxySetting_Authentication","1");
                props.put("MobileProxySetting_UserName",proxySettings.getFsProxyUserName());
                props.put("MobileProxySetting_Password",proxySettings.getFsProxyPassword());
            }else{
                props.put("MobileProxySetting_Authentication","0");
                props.put("MobileProxySetting_UserName","");
                props.put("MobileProxySetting_Password","");
            }
        }else{
            props.put("MobileUseProxy", "0");
            props.put("MobileProxyType","0");
            props.put("MobileProxySetting_Authentication","0");
            props.put("MobileProxySetting_Address", "");
            props.put("MobileProxySetting_UserName","");
            props.put("MobileProxySetting_Password","");
        }

        if(useSSL){
            props.put("MobileUseSSL","1");
        }else{
            props.put("MobileUseSSL","0");
        }
        return props;
    }

    public JSONObject getJobDetails(String mcUrl, String proxyAddress, String proxyUserName, String proxyPassword){
        if(StringUtils.isBlank(fsUserName) || StringUtils.isBlank(fsPassword.getPlainText())){
            return null;
        }
        return JobConfigurationProxy.getInstance().getJobById(mcUrl, fsUserName, fsPassword.getPlainText(), proxyAddress, proxyUserName, proxyPassword, fsJobId);
    }
}
