// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import hudson.ExtensionPoint;

public interface ConfigurationListener extends ExtensionPoint {

    void onChanged(ServerConfiguration conf);

}
