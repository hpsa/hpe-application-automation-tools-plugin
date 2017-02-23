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

package com.hp.octane.plugins.jenkins.tests.build;

public final class BuildDescriptor {

	private final String jobId;
	private final String jobName;
	private final String buildId;
	private final String buildName;
	private final String subType;

	public BuildDescriptor(String jobId, String jobName, String buildId, String buildName, String subType) {
		this.jobId = jobId;
		this.jobName = jobName;
		this.buildId = buildId;
		this.buildName = buildName;
		this.subType = subType;
	}

	public String getJobId() {
		return jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public String getBuildId() {
		return buildId;
	}

	public String getBuildName() {
		return buildName;
	}

	public String getSubType() {
		return subType;
	}
}
