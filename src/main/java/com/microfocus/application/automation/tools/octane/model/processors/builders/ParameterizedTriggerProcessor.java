/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.model.processors.builders;

import com.microfocus.application.automation.tools.octane.model.ModelFactory;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation for discovery/provisioning of an internal phases/steps of the specific Job in context of ParameterizedTrigger Plugin
 */
public class ParameterizedTriggerProcessor extends AbstractBuilderProcessor {

	ParameterizedTriggerProcessor(Builder builder, Job job, String phasesName, Set<Job> processedJobs) {
		TriggerBuilder b = (TriggerBuilder) builder;
		super.phases = new ArrayList<>();
		List<AbstractProject> items;
		for (BlockableBuildTriggerConfig config : b.getConfigs()) {
			items = config.getProjectList(job.getParent(), null);
			eliminateIllegalItems(job, processedJobs, items);
			super.phases.add(ModelFactory.createStructurePhase(phasesName, config.getBlock() != null, items, processedJobs));
		}
	}

	public ParameterizedTriggerProcessor(Publisher publisher, AbstractProject project, String phasesName, Set<Job> processedJobs) {
		BuildTrigger t = (BuildTrigger) publisher;
		super.phases = new ArrayList<>();
		List<AbstractProject> items;
		for (BuildTriggerConfig config : t.getConfigs()) {
			items = config.getProjectList(project.getParent(), null);
			eliminateIllegalItems(project, processedJobs, items);
			super.phases.add(ModelFactory.createStructurePhase(phasesName, false, items, processedJobs));
		}
	}
}
