// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.client;

public interface MqmRestClientFactory {

    MqmRestClient create(String location, String domain, String project, String username, String password);

}
