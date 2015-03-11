// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins;

import com.hp.octane.plugins.jenkins.identity.IdentityProvider;
import hudson.Plugin;

import java.io.IOException;
import java.util.UUID;

public class OctanePlugin extends Plugin implements IdentityProvider {

    private String identity;

    @Override
    public String getIdentity() {
        return identity;
    }

    @Override
    public void postInitialize() throws IOException {
        if (identity == null) {
            this.identity = UUID.randomUUID().toString();
            save();
        }
    }
}
