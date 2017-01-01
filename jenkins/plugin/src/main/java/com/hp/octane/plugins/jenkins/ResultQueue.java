// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins;

import java.io.Serializable;

public interface ResultQueue {

    ResultQueue.QueueItem peekFirst();

    boolean failed();

    void remove();

    void add(String projectName, int buildNumber);

    void add(String projectName, int buildNumber, String workspace);

    class QueueItem implements Serializable {
        String projectName;
        int buildNumber;
        String workspace;
        int failCount;

        public QueueItem(String projectName, int buildNumber) {
            this(projectName, buildNumber, 0);
        }

        public QueueItem(String projectName, int buildNumber, String workspace) {
            this(projectName, buildNumber, 0);
            this.workspace = workspace;
        }

        QueueItem(String projectName, int buildNumber, int failCount) {
            this.projectName = projectName;
            this.buildNumber = buildNumber;
            this.failCount = failCount;
        }

        QueueItem(String projectName, int buildNumber, int failCount, String workspace) {
            this.projectName = projectName;
            this.buildNumber = buildNumber;
            this.failCount = failCount;
            this.workspace = workspace;
        }

        public int incrementFailCount() {
            return this.failCount++;
        }

        public int getFailCount() {
            return failCount;
        }

        public String getProjectName() {
            return projectName;
        }

        public int getBuildNumber(){
            return buildNumber;
        }

        public String getWorkspace() {
            return workspace;
        }
    }
}
