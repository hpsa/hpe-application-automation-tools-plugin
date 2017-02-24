// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.client;

import com.hp.mqm.client.MqmRestClient;
import hudson.util.Secret;

public interface JenkinsMqmRestClientFactory {

    MqmRestClient obtain(String location, String sharedSpace, String username, Secret password);

    MqmRestClient obtainTemp(String location, String sharedSpace, String username, Secret password);

    void updateMqmRestClient(String location, String sharedSpace, String username, Secret password);

}
