package com.hpe.application.automation.tools.model;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 10/13/15
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class MCServerSettingsModel {

    private final String _mcServerName;
    private final String _mcServerUrl;

    @DataBoundConstructor
    public MCServerSettingsModel(String mcServerName, String mcServerUrl) {

        _mcServerName = mcServerName;
        _mcServerUrl = mcServerUrl;
    }

    /**
     * @return the mcServerName
     */
    public String getMcServerName() {

        return _mcServerName;
    }

    /**
     * @return the mcServerUrl
     */
    public String getMcServerUrl() {

        return _mcServerUrl;
    }

    public Properties getProperties() {

        Properties prop = new Properties();
        if (!StringUtils.isEmpty(_mcServerUrl)) {
            prop.put("MobileHostAddress", _mcServerUrl);
        } else {
            prop.put("MobileHostAddress", "");
        }

        return prop;
    }
}
