// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.octane.plugins.jenkins.Messages;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Item;
import org.kohsuke.stapler.StaplerProxy;

public class ConfigurationAction implements Action, StaplerProxy {

    final public AbstractProject owner;
    final public JobConfigurationProxy proxy;

    public ConfigurationAction(AbstractProject project) {
        this.owner = project;
        this.proxy = new JobConfigurationProxy(project);
    }

    @Override
    public String getIconFileName() {
        return owner.getACL().hasPermission(Item.CONFIGURE)? "setting.png": null;
    }

    @Override
    public String getDisplayName() {
        return Messages.ConfigurationLabel();
    }

    @Override
    public String getUrlName() {
        return "mqmConfiguration";
    }

    @Override
    public Object getTarget() {
        owner.getACL().checkPermission(Item.CONFIGURE);
        return this;
    }
}
