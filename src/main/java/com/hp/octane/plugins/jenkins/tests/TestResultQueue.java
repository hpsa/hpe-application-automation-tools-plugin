// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.hp.octane.plugins.jenkins.ResultQueueImpl;
import hudson.Extension;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.IOException;

@Extension
public class TestResultQueue extends ResultQueueImpl {

    public TestResultQueue() throws IOException {
        File queueFile = new File(Jenkins.getInstance().getRootDir(), "octane-test-result-queue.dat");
        init(queueFile);
    }

    /*
     * To be used in tests only.
     */
    TestResultQueue(File queueFile) throws IOException {
        init(queueFile);
    }
}
