// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.identity;

import com.hp.octane.plugins.jenkins.OctaneJenkinsPlugin;
import jenkins.model.Jenkins;

public class ServerIdentity {

    public static String getIdentity() {
        return Jenkins.getInstance().getPlugin(OctaneJenkinsPlugin.class).getIdentity();
    }
}
