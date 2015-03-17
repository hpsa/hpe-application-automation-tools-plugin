// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.client;

import hudson.Extension;

@Extension
public class MqmRestClientFactoryImpl implements MqmRestClientFactory {

    @Override
    public MqmRestClient create(String location, String domain, String project, String username, String password) {
        return new MqmRestClientImpl(location, domain, project, username, password);
    }
}
