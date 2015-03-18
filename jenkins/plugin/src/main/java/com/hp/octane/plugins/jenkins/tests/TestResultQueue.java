// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import java.io.Serializable;

public interface TestResultQueue {

    boolean isEmpty();

    TestResultQueue.QueueItem removeFirst();

    void add(String projectName, int buildNumber);

    class QueueItem implements Serializable {
        String projectName;
        int buildNumber;

        QueueItem(String projectName, int buildNumber) {
            this.projectName = projectName;
            this.buildNumber = buildNumber;
        }
    }
}
