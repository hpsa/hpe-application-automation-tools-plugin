/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane;

import java.io.Serializable;

@SuppressWarnings("squid:S2039")
public interface ResultQueue {

    ResultQueue.QueueItem peekFirst();

    boolean failed();

    void remove();

    void add(String projectName, int buildNumber);

    void add(String projectName, int buildNumber, String workspace);

    void clear();

    class QueueItem implements Serializable {
        private static final long serialVersionUID = 1;

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
