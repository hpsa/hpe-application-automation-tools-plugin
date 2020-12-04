/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


public class AlmServerSettingsModel extends AbstractDescribableImpl<AlmServerSettingsModel> {
    
    private final String _almServerName;
    private final String _almServerUrl;
    private List<CredentialsModel> _almCredentials;
    private List<SSOCredentialsModel> _almSSOCredentials;

    @DataBoundConstructor
    public AlmServerSettingsModel(String almServerName, String almServerUrl,
                                  List<CredentialsModel> almCredentials,
                                  List<SSOCredentialsModel> almSSOCredentials) {
        
        this._almServerName = almServerName.trim();
        this._almServerUrl = almServerUrl.trim();
        this._almCredentials = almCredentials;
        this._almSSOCredentials = almSSOCredentials;
    }

    /**
     * @return the almServerName
     */
    public String getAlmServerName() {
        
        return _almServerName;
    }
    
    /**
     * @return the almServerUrl
     */
    public String getAlmServerUrl() {
        
        return _almServerUrl;
    }

    public List<CredentialsModel> getAlmCredentials(){
        if(_almCredentials != null) {
            return _almCredentials;
        }

        return new ArrayList<>();
    }

    public void set_almCredentials(List<CredentialsModel> almCredentials){
        this._almCredentials = almCredentials;
    }

    public List<SSOCredentialsModel> getAlmSSOCredentials() {
        if (_almSSOCredentials != null) {
            return _almSSOCredentials;
        }

        return new ArrayList<>();
    }

    public void set_almSSOCredentials(List<SSOCredentialsModel> almSSOCredentials){
        this._almSSOCredentials = almSSOCredentials;
    }

    public Properties getProperties() {
        
        Properties prop = new Properties();

        if (!StringUtils.isEmpty(_almServerUrl)) {
            prop.put("almServerUrl", _almServerUrl.trim());
        } else {
            prop.put("almServerUrl", "");
        }
        return prop;
    }


    @Extension
    public static class DescriptorImpl extends Descriptor<AlmServerSettingsModel> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Alm Server Settings Model";
        }
    }
}
