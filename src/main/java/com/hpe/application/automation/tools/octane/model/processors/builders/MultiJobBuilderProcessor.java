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

package com.hpe.application.automation.tools.octane.model.processors.builders;

import com.hpe.application.automation.tools.octane.model.ModelFactory;
import com.tikal.jenkins.plugins.multijob.MultiJobBuilder;
import com.tikal.jenkins.plugins.multijob.PhaseJobsConfig;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation for discovery/provisioning of an internal phases/steps of the specific Job in context of MultiJob Plugin
 */
public class MultiJobBuilderProcessor extends AbstractBuilderProcessor {
	private static final Logger logger = LogManager.getLogger(MultiJobBuilderProcessor.class);

	public MultiJobBuilderProcessor(Builder builder, Set<Job> processedJobs) {
		MultiJobBuilder b = (MultiJobBuilder) builder;
		super.phases = new ArrayList<>();
		List<AbstractProject> items = new ArrayList<>();
		AbstractProject tmpProject;
		for (PhaseJobsConfig config : b.getPhaseJobs()) {
			tmpProject = (AbstractProject) Jenkins.getInstance().getItem(config.getJobName());
			if (tmpProject == null) {
				logger.warn("project named '" + config.getJobName() + "' not found; considering this as corrupted configuration and skipping the project");
			} else if (processedJobs.contains(tmpProject)) {
				logger.warn("project named '" + config.getJobName() + "' is duplicated");
			} else {
				items.add(tmpProject);
			}
		}
		super.phases.add(ModelFactory.createStructurePhase(b.getPhaseName(), true, items));
	}
}
