// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.identity;

public interface IdentityProvider {

    /**
     * Get identifier of the build server instance
     * @return identity
     */
    public String getIdentity();

}
