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

package com.hpe.application.automation.tools.octane.model.processors.projects;

import hudson.maven.MavenModuleSet;
import hudson.model.Job;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation for discovery/provisioning of an internal phases/steps of the specific Job in context of Maven Plugin
 */
class MavenProjectProcessor extends AbstractProjectProcessor<MavenModuleSet> {

	MavenProjectProcessor(Job mavenJob, Set<Job> processedJobs) {
		super((MavenModuleSet) mavenJob);
		//  Internal phases - pre maven phases
		//
		super.processBuilders(this.job.getPrebuilders(), this.job, "pre-maven", processedJobs);

		//  Internal phases - post maven phases
		//
		super.processBuilders(this.job.getPostbuilders(), this.job, "post-maven", processedJobs);

		//  Post build phases
		//
		super.processPublishers(this.job, processedJobs);
	}

	@Override
	public List<Builder> tryGetBuilders() {
		return new ArrayList<>();
	}

	@Override
	public void scheduleBuild(String parametersBody) {
		throw new RuntimeException("non yet implemented");
	}
}
