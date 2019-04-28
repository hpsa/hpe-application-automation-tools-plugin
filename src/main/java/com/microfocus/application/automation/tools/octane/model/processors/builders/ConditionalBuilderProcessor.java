/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.model.processors.builders;

import com.hp.octane.integrations.dto.pipelines.PipelinePhase;
import hudson.model.Job;
import hudson.tasks.BuildStep;
import hudson.tasks.Builder;
import org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder;

import java.util.List;
import java.util.Set;

/**
 * Implementation for discovery/provisioning of an internal phases/steps of the specific Job in context of Conditional Plugin (parent definitions)
 */
class ConditionalBuilderProcessor extends AbstractBuilderProcessor {

	ConditionalBuilderProcessor(Builder builder, Job job, String phasesName, List<PipelinePhase> internalPhases, Set<Job> processedJobs) {
		ConditionalBuilder conditionalBuilder = (ConditionalBuilder) builder;
		for (BuildStep currentBuildStep : conditionalBuilder.getConditionalbuilders()) {
			processInternalBuilders((Builder) currentBuildStep, job, phasesName, internalPhases, processedJobs);
		}
	}
}
