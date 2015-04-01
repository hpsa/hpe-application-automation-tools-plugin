// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import java.io.Serializable;

public interface TestResultQueue {

    TestResultQueue.QueueItem peekFirst();

    boolean failed();

    void remove();

    void add(String projectName, int buildNumber);

    class QueueItem implements Serializable {
        String projectName;
        int buildNumber;
        int failCount;

        QueueItem(String projectName, int buildNumber) {
            this(projectName, buildNumber, 0);
        }

        QueueItem(String projectName, int buildNumber, int failCount) {
            this.projectName = projectName;
            this.buildNumber = buildNumber;
            this.failCount = failCount;
        }
    }
}
