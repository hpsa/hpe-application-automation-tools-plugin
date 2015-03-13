// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.octane.plugins.jenkins.Messages;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.client.MqmRestClientImpl;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;

public class ConfigurationService {

    public static ServerConfiguration getServerConfiguration() {
        OctanePlugin octanePlugin = Jenkins.getInstance().getPlugin(OctanePlugin.class);
        return new ServerConfiguration(octanePlugin.getLocation(), octanePlugin.getUsername(), octanePlugin.getPassword());
    }

    public static FormValidation checkConfiguration(String location, String username, String password) {
        MqmRestClientImpl client = new MqmRestClientImpl(location, username, password);
        if (client.login()) {
            return FormValidation.okWithMarkup("<font color=\"green\"><b>" + Messages.ConnectionSuccess() + "</b></font>");
        } else {
            return FormValidation.errorWithMarkup("<font color=\"red\"><b>" + Messages.ConnectionFailure() + "</b></font>");
        }
    }
}
