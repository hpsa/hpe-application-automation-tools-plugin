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

package com.microfocus.application.automation.tools.settings;

import com.microfocus.application.automation.tools.model.MCServerSettingsModel;
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.XmlFile;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.Serializable;

/**
 * @author jingwei
 */
@Extension(ordinal = 1)
public class MCServerSettingsGlobalConfiguration extends GlobalConfiguration implements Serializable {

    public MCServerSettingsGlobalConfiguration(){
        load();
    }

    public static MCServerSettingsGlobalConfiguration getInstance() {
        return GlobalConfiguration.all().get(MCServerSettingsGlobalConfiguration.class);
    }

    @Override
    protected XmlFile getConfigFile() {
        XmlFile xmlFile = super.getConfigFile();
        ConfigurationMigrationUtil.migrateConfigurationFileIfRequired(xmlFile,
                "com.microfocus.application.automation.tools.settings.MCServerSettingsBuilder.xml",
                "MCServerSettingsBuilder_-MCDescriptorImpl",
                "MCServerSettingsGlobalConfiguration");

        return xmlFile;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        setInstallations(req.bindParametersToList(MCServerSettingsModel.class, "mc.").toArray(
                new MCServerSettingsModel[0]));

        save();

        return super.configure(req, formData);
    }

    public FormValidation doCheckMCServerName(@QueryParameter String value) {
        FormValidation ret = FormValidation.ok();
        if (StringUtils.isBlank(value)) {
            ret = FormValidation.error("UFT Mobile server name cannot be empty");
        }

        return ret;
    }

    public FormValidation doCheckMCServerURL(@QueryParameter String value) {
        FormValidation ret = FormValidation.ok();
        if (StringUtils.isBlank(value)) {
            ret = FormValidation.error("UFT Mobile server cannot be empty");
        }

        return ret;
    }

    @CopyOnWrite
    private MCServerSettingsModel[] installations = new MCServerSettingsModel[0];

    public MCServerSettingsModel[] getInstallations() {
        return installations;
    }

    public void setInstallations(MCServerSettingsModel... installations) {
        this.installations = installations;
    }

    public Boolean hasMCServers() {
        return installations.length > 0;
    }

}
