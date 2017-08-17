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
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Publisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implementation for discovery/provisioning of an internal phases/steps of the specific Job in context of BuildTrigger
 */
public class BuildTriggerProcessor extends AbstractBuilderProcessor {
	private static final Logger logger = LogManager.getLogger(BuildTriggerProcessor.class);

	public BuildTriggerProcessor(Publisher publisher, AbstractProject project, Set<Job> processedJobs) {
		BuildTrigger t = (BuildTrigger) publisher;
		super.phases = new ArrayList<>();
		List<AbstractProject> items = t.getChildProjects(project.getParent());
		for (Iterator<AbstractProject> iterator = items.iterator(); iterator.hasNext(); ) {
			AbstractProject next = iterator.next();
			if (next == null || processedJobs.contains(next)) {
				iterator.remove();
				logger.warn("encountered null project reference; considering it as corrupted configuration and skipping");
			}
		}
		super.phases.add(ModelFactory.createStructurePhase("downstream", false, items));
	}
}
