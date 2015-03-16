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
        return new ServerConfiguration(
                octanePlugin.getLocation(),
                octanePlugin.getDomain(),
                octanePlugin.getProject(),
                octanePlugin.getUsername(),
                octanePlugin.getPassword());
    }

    public static FormValidation checkConfiguration(String location, String domain, String project, String username, String password) {
        MqmRestClientImpl client = new MqmRestClientImpl(location, domain, project, username, password);
        if (!client.login()) {
            return FormValidation.errorWithMarkup(markup("red", Messages.ConnectionFailure()));
        }
        if (!client.createSession()) {
            return FormValidation.errorWithMarkup(markup("red", Messages.ConnectionSessionFailure()));
        }
        if (!client.checkDomainAndProject()) {
            return FormValidation.errorWithMarkup(markup("red", Messages.ConnectionDomainProjectInvalid()));
        }
        return FormValidation.okWithMarkup(markup("green", Messages.ConnectionSuccess()));
    }

    private static String markup(String color, String message) {
        return "<font color=\"" + color + "\"><b>" + message + "</b></font>";
    }
}
