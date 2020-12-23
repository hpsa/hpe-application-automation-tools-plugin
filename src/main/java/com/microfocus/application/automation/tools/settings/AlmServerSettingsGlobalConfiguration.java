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

import com.microfocus.application.automation.tools.model.AlmServerSettingsModel;
import com.microfocus.application.automation.tools.model.CredentialsModel;
import com.microfocus.application.automation.tools.model.SSOCredentialsModel;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.XmlFile;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Extension(ordinal = 1)
public class AlmServerSettingsGlobalConfiguration extends GlobalConfiguration implements Serializable {

    public AlmServerSettingsGlobalConfiguration(){
        load();
    }

    public static AlmServerSettingsGlobalConfiguration getInstance() {
        return GlobalConfiguration.all().get(AlmServerSettingsGlobalConfiguration.class);
    }

    @Override
    protected XmlFile getConfigFile() {
        XmlFile xmlFile = super.getConfigFile();
        ConfigurationMigrationUtil.migrateConfigurationFileIfRequired(xmlFile,
                "com.microfocus.application.automation.tools.settings.AlmServerSettingsBuilder.xml",
                "AlmServerSettingsBuilder_-DescriptorImpl",
                "AlmServerSettingsGlobalConfiguration");

        return xmlFile;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        List<AlmServerSettingsModel> models = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();

        Object data = formData.get("inst");
        if (data instanceof JSONObject) {
            jsonArray.add(data);
        } else if (data instanceof JSONArray) {
            jsonArray.addAll((JSONArray) data);
        }


        for (Object jsonObject : jsonArray) {
            JSONObject json = (JSONObject) jsonObject;
            Object credentialsObj = json.get("credentials");
            List<CredentialsModel> credentials = new ArrayList<>();
            if (credentialsObj instanceof JSONArray) {
                JSONArray credentialsObjArray = (JSONArray) credentialsObj;
                credentials = req.bindJSONToList(CredentialsModel.class, credentialsObjArray);
            } else if (credentialsObj instanceof JSONObject) {
                CredentialsModel credentialsModel = req.bindJSON(CredentialsModel.class, (JSONObject) credentialsObj);
                credentials.add(credentialsModel);
            }

            Object ssoCredentialsObj = json.get("ssoCredentials");
            List<SSOCredentialsModel> ssoCredentials = new ArrayList<>();
            if (ssoCredentialsObj instanceof JSONArray) {
                JSONArray ssoCredentialsObjArray = (JSONArray) ssoCredentialsObj;
                ssoCredentials = req.bindJSONToList(SSOCredentialsModel.class, ssoCredentialsObjArray);
            } else if (ssoCredentialsObj instanceof JSONObject) {
                SSOCredentialsModel ssoCredentialsModel = req.bindJSON(SSOCredentialsModel.class, (JSONObject) ssoCredentialsObj);
                ssoCredentials.add(ssoCredentialsModel);
            }

            AlmServerSettingsModel newModel = req.bindJSON(AlmServerSettingsModel.class, json);
            newModel.set_almCredentials(credentials);
            newModel.set_almSSOCredentials(ssoCredentials);
            if (!StringUtils.isEmpty(newModel.getAlmServerName()) && !(StringUtils.isEmpty(newModel.getAlmServerUrl()))) {
                models.add(newModel);
            }
        }

        setInstallations(models.toArray(new AlmServerSettingsModel[0]));

        save();

        return super.configure(req, formData);
    }

    public FormValidation doCheckAlmServerUrl(@QueryParameter String value) {
        return checkQcServerURL(value, false);
    }

    @CopyOnWrite
    private AlmServerSettingsModel[] installations = new AlmServerSettingsModel[0];

    public AlmServerSettingsModel[] getInstallations() {
        return installations;
    }

    public void setInstallations(AlmServerSettingsModel... installations) {
        this.installations = installations;
    }

    public FormValidation doCheckAlmServerName(@QueryParameter String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.error("ALM server name cannot be empty");
        }

        List<AlmServerSettingsModel> models = Arrays.asList(getInstallations());
        for (AlmServerSettingsModel model : models) {
            if (model.getAlmServerName().equals(value) && model.getAlmCredentials().isEmpty() && model.getAlmSSOCredentials().isEmpty()) {
                return FormValidation.error("Alm server does not have credentials defined");
            }
        }

        return FormValidation.ok();
    }

    private FormValidation checkQcServerURL(String value, Boolean acceptEmpty) {
        String url;
        // Path to the page to check if the server is alive
        String page = "servlet/tdservlet/TDAPI_GeneralWebTreatment";

        // Do will allow empty value?
        if (StringUtils.isBlank(value)) {
            if (!acceptEmpty) {
                return FormValidation.error("ALM server must be defined");
            } else {
                return FormValidation.ok();
            }
        }

        // Does the URL ends with a "/" ? if not, add it
        if (value.lastIndexOf('/') == value.length() - 1) {
            url = value + page;
        } else {
            url = value + "/" + page;
        }

        // Open the connection and perform a HEAD request
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            // Check the response code
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return FormValidation.error(connection.getResponseMessage());
            }
        } catch (MalformedURLException ex) {
            // This is not a valid URL
            return FormValidation.error("ALM server URL is malformed.");
        } catch (IOException ex) {
            // Cant open connection to the server
            return FormValidation.error("Error opening a connection to the ALM server");
        }

        return FormValidation.ok();
    }


    public FormValidation doCheckAlmUsername(@QueryParameter String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.error("Username must be set");
        }

        return FormValidation.ok();
    }

    public FormValidation doCheckAlmClientID(@QueryParameter String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.error("Client ID must be set");
        }

        return FormValidation.ok();
    }

    @SuppressWarnings("unused")
    private FormValidation doCheckAlmCredentials(@QueryParameter List<CredentialsModel> almCredentials) {
        if (almCredentials.isEmpty()) {
            return FormValidation.error("Am server does not have credentials defined");
        }
        return FormValidation.ok();
    }

    public Boolean hasAlmServers() {
        return installations.length > 0;
    }

}
