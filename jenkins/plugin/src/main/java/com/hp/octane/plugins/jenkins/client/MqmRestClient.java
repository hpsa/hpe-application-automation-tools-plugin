// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.client;

import java.io.File;
import java.io.IOException;

public interface MqmRestClient {

    boolean login();

    boolean createSession();

    boolean checkDomainAndProject();

    int post(String projectPath, File file, String contentType) throws IOException;
}
