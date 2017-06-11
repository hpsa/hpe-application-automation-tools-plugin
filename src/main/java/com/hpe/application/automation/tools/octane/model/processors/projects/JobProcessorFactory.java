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

import hudson.model.Job;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gadiel on 30/11/2016.
 *
 * Job processors factory - should be used as a 'static' class, no instantiation, only static method/s
 */

public class JobProcessorFactory {

	private JobProcessorFactory() {
	}

	public static <T extends Job> AbstractProjectProcessor<T> getFlowProcessor(T job){
		Set<Job> processedJobs = new HashSet<>();
		return getFlowProcessor(job, processedJobs);
	}

	private static <T extends Job> AbstractProjectProcessor<T> getFlowProcessor(T job, Set<Job> processedJobs) {
		AbstractProjectProcessor flowProcessor;
		processedJobs.add(job);

		if (job.getClass().getName().equals("hudson.model.FreeStyleProject")) {
			flowProcessor = new FreeStyleProjectProcessor(job, processedJobs);
		} else if (job.getClass().getName().equals("hudson.matrix.MatrixProject")) {
			flowProcessor = new MatrixProjectProcessor(job, processedJobs);
		} else if (job.getClass().getName().equals("hudson.maven.MavenModuleSet")) {
			flowProcessor = new MavenProjectProcessor(job, processedJobs);
		} else if (job.getClass().getName().equals("com.tikal.jenkins.plugins.multijob.MultiJobProject")) {
			flowProcessor = new MultiJobProjectProcessor(job, processedJobs);
		} else if (job.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
			flowProcessor = new WorkFlowJobProcessor(job);
		} else {
			flowProcessor = new UnsupportedProjectProcessor(job);
		}
		return flowProcessor;
	}
}
