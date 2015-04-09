// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.octane.plugins.jenkins.Messages;
import hudson.model.AbstractProject;
import hudson.model.Action;

public class ConfigurationAction implements Action {

    final public AbstractProject owner;
    final public JobConfigurationProxy proxy;

    public ConfigurationAction(AbstractProject project) {
        this.owner = project;
        this.proxy = new JobConfigurationProxy(project);
    }

    @Override
    public String getIconFileName() {
        return "setting.png";
    }

    @Override
    public String getDisplayName() {
        return Messages.ConfigurationLabel();
    }

    @Override
    public String getUrlName() {
        return "mqmConfiguration";
    }
}
