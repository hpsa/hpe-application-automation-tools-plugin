/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.octane.vulnerabilities;

import java.io.Serializable;

@SuppressWarnings("squid:S2039")
public interface ScanResultQueue {

    ScanResultQueue.QueueItem peekFirst();

    boolean failed();

    void remove();

    void add(String buildId, String jobId, String projectName, String ProjectVersion);

    void clear();

    class QueueItem implements Serializable {
        private static final long serialVersionUID = 1;
        private String buildId;
        private String jobId;
        private String projectName;
        private String ProjectVersion;

        public QueueItem(String buildId, String jobId, String projectName, String projectVersion) {
            this.buildId = buildId;
            this.jobId = jobId;
            this.projectName = projectName;
            ProjectVersion = projectVersion;
        }

        public String getBuildId() {
            return buildId;
        }

        public void setBuildId(String buildId) {
            this.buildId = buildId;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getProjectVersion() {
            return ProjectVersion;
        }

        public void setProjectVersion(String projectVersion) {
            ProjectVersion = projectVersion;
        }


    }
}
