// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.octane.plugins.jenkins.Messages;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import org.kohsuke.stapler.StaplerProxy;

public class ConfigurationAction implements Action, StaplerProxy {

    final public Job owner;
    final public JobConfigurationProxy proxy;

    public ConfigurationAction(Job job) {
        this.owner = job;
        this.proxy = new JobConfigurationProxy(job);
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
