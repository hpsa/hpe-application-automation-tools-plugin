// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.octane.plugins.jenkins.OctanePlugin;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationService {

    public static ServerConfiguration getServerConfiguration() {
        OctanePlugin octanePlugin = Jenkins.getInstance().getPlugin(OctanePlugin.class);
        return new ServerConfiguration(octanePlugin.getLocation(), octanePlugin.getUsername(), octanePlugin.getPassword());
    }

    public static FormValidation checkConfiguration(String location, String username, String password) {
        return login(location, username, password);
    }

    private static FormValidation login(String location, String username, String password) {
        // TODO: janotav: temporary code (do we need mqm-rest-client?)
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        List<String> authPrefs = new ArrayList<String>(2);
        authPrefs.add(AuthPolicy.DIGEST);
        authPrefs.add(AuthPolicy.BASIC);
        httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

        String authPoint = location +  "/authentication-point/alm-authenticate"; // NON-NLS
        String xml = "<alm-authentication><user>" + username + "</user><password>" + password + "</password></alm-authentication>";

        try {
            PostMethod authenticate = new PostMethod(authPoint);
            authenticate.setRequestEntity(new StringRequestEntity(xml, "application/xml", "UTF-8"));
            int code = httpClient.executeMethod(authenticate);
            if (code == 200) {
                return FormValidation.okWithMarkup("<font color=\"green\"><b>Connection successful</b></font>");
            } else {
                return FormValidation.errorWithMarkup("<font color=\"red\"><b>Unable to authenticate</b></font>");
            }
        } catch (IllegalArgumentException e) {
            return FormValidation.errorWithMarkup("<font color=\"red\"><b>Failed to connect, check your configuration</b></font>");
        } catch (IOException e) {
            return FormValidation.errorWithMarkup("<font color=\"red\"><b>Failed to connect to the server</b></font>");
        }
    }
}
