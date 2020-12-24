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

import com.microfocus.application.automation.tools.model.SvServerSettingsModel;
import com.microfocus.sv.svconfigurator.core.impl.jaxb.ServerInfo;
import com.microfocus.sv.svconfigurator.core.impl.processor.Credentials;
import com.microfocus.sv.svconfigurator.serverclient.ICommandExecutor;
import com.microfocus.sv.svconfigurator.serverclient.impl.CommandExecutorFactory;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Extension(ordinal = 1)
public class SvServerSettingsGlobalConfiguration extends GlobalConfiguration implements Serializable {

    public static SvServerSettingsGlobalConfiguration getInstance() {
        return GlobalConfiguration.all().get(SvServerSettingsGlobalConfiguration.class);
    }

    @Override
    protected XmlFile getConfigFile() {
        XmlFile xmlFile = super.getConfigFile();
        ConfigurationMigrationUtil.migrateConfigurationFileIfRequired(xmlFile,
                "com.microfocus.application.automation.tools.settings.SvServerSettingsBuilder.xml",
                "SvServerSettingsBuilder_-DescriptorImpl",
                "SvServerSettingsGlobalConfiguration");

        return xmlFile;
    }

    @CopyOnWrite
    private SvServerSettingsModel[] servers;

    public SvServerSettingsGlobalConfiguration() {
        load();
    }

    private static boolean isHttpsSchema(String url) {
        try {
            return "https".equals(new URL(url).getProtocol());
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        List<SvServerSettingsModel> list = req.bindJSONToList(SvServerSettingsModel.class, formData.get("srv"));

        validateMandatoryFields(list);

        servers = list.toArray(new SvServerSettingsModel[list.size()]);

        save();

        return super.configure(req, formData);
    }

    private void validateMandatoryFields(List<SvServerSettingsModel> servers) throws FormException {
        for (SvServerSettingsModel server : servers) {
            validateConfiguration(doCheckName(server.getName()), "name");
            validateConfiguration(doCheckUrl(server.getUrl()), "url");
            validateConfiguration(doCheckUsername(server.getUsername(), server.getUrl()), "username");
            validateConfiguration(doCheckPassword(server.getPassword(), server.getUrl()), "password");
        }
    }

    private void validateConfiguration(FormValidation result, String formField) throws FormException {
        if (!result.equals(FormValidation.ok())) {
            throw new FormException("Validation of property in Service Virtualization server configuration failed: " + result.getMessage(), formField);
        }
    }

    public FormValidation doCheckUrl(@QueryParameter String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.error("Management Endpoint URL cannot be empty");
        }
        try {
            new URL(value);
        } catch (MalformedURLException e) {
            return FormValidation.error("'" + value + "' is not valid URL");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckUsername(@QueryParameter String value, @QueryParameter("url") final String url) {
        if (isHttpsSchema(url) && StringUtils.isBlank(value)) {
            return FormValidation.error("Username is required for secured server");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckPassword(@QueryParameter String value, @QueryParameter("url") final String url) {
        if (isHttpsSchema(url) && StringUtils.isBlank(value)) {
            return FormValidation.error("Password is required for secured server");
        }
        return FormValidation.ok();
    }

    @SuppressWarnings("unused")
    public FormValidation doTestConnection(@QueryParameter("url") final String url, @QueryParameter("username") final String username,
                                           @QueryParameter("password") final String password) {
        try {
            Credentials credentials = (!StringUtils.isBlank(username)) ? new Credentials(username, password) : null;
            ICommandExecutor commandExecutor = new CommandExecutorFactory().createCommandExecutor(new URL(url), credentials);
            ServerInfo serverInfo = commandExecutor.getClient().getServerInfo();
            return FormValidation.ok("Validation passed. Connected to %s server of version: %s", serverInfo.getServerType(), serverInfo.getProductVersion());
        } catch (Exception e) {
            return FormValidation.error("Validation failed: " + e.getMessage());
        }
    }

    public SvServerSettingsModel[] getServers() {
        return servers;
    }

    public void setServers(SvServerSettingsModel[] servers) {
        this.servers = servers;
    }

    public FormValidation doCheckName(@QueryParameter String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.error("Name cannot be empty");
        }

        return FormValidation.ok();
    }
}
