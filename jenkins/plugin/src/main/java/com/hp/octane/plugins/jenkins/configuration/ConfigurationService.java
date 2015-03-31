// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.google.inject.Inject;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.AuthenticationException;
import com.hp.mqm.client.exception.DomainProjectNotExistException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.SessionCreationException;
import com.hp.octane.plugins.jenkins.Messages;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;

import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class ConfigurationService {

    private final static Logger logger = Logger.getLogger(ConfigurationService.class.getName());

    private JenkinsMqmRestClientFactory clientFactory;

    public static ServerConfiguration getServerConfiguration() {
        OctanePlugin octanePlugin = Jenkins.getInstance().getPlugin(OctanePlugin.class);
        return octanePlugin.getServerConfiguration();
    }

    public FormValidation checkConfiguration(String location, String domain, String project, String username, String password) {
        MqmRestClient client = clientFactory.create(location, domain, project, username, password);
        try {
            client.tryToConnectProject();
        } catch (AuthenticationException e) {
            return FormValidation.errorWithMarkup(markup("red", Messages.AuthenticationFailure()));
        } catch (SessionCreationException e) {
            return FormValidation.errorWithMarkup(markup("red", Messages.SessionCreationFailure()));
        } catch (DomainProjectNotExistException e) {
            return FormValidation.errorWithMarkup(markup("red", Messages.ConnectionDomainProjectInvalid()));
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Connection check failed due to communication problem.", e);
            return FormValidation.errorWithMarkup(markup("red", Messages.ConnectionFailure()));
        }
        return FormValidation.okWithMarkup(markup("green", Messages.ConnectionSuccess()));
    }

    private static String markup(String color, String message) {
        return "<font color=\"" + color + "\"><b>" + message + "</b></font>";
    }

    @Inject
    public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;
    }

    /*
     * To be used in tests only.
     */
    public void _setMqmRestClientFactory(JenkinsMqmRestClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
}
