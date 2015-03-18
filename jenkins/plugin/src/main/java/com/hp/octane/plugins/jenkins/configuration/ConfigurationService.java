// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.Messages;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.client.MqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.MqmRestClientFactoryImpl;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;

@Extension
public class ConfigurationService {

    private MqmRestClientFactory clientFactory;

    public static ServerConfiguration getServerConfiguration() {
        OctanePlugin octanePlugin = Jenkins.getInstance().getPlugin(OctanePlugin.class);
        return octanePlugin.getServerConfiguration();
    }

    public FormValidation checkConfiguration(String location, String domain, String project, String username, String password) {
        MqmRestClient client = clientFactory.create(location, domain, project, username, password);
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

    @Inject
    public void setMqmRestClientFactory(MqmRestClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;
    }

    /*
     * To be used in tests only.
     */
    public void _setMqmRestClientFactory(MqmRestClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
}
