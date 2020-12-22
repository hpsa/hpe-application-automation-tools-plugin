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

import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.model.ModelFactory;
import com.tikal.jenkins.plugins.multijob.MultiJobBuilder;
import com.tikal.jenkins.plugins.multijob.PhaseJobsConfig;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation for discovery/provisioning of an internal phases/steps of the specific Job in context of MultiJob Plugin
 */
class MultiJobBuilderProcessor extends AbstractBuilderProcessor {
    private static final Logger logger = SDKBasedLoggerProvider.getLogger(MultiJobBuilderProcessor.class);

    MultiJobBuilderProcessor(Builder builder, Job job, Set<Job> processedJobs) {
        MultiJobBuilder b = (MultiJobBuilder) builder;
        super.phases = new ArrayList<>();
        List<AbstractProject> items = new ArrayList<>();
        AbstractProject tmpProject;
        for (PhaseJobsConfig config : b.getPhaseJobs()) {
            if(StringUtils.isEmpty(config.getJobName())){
                continue;
            }
            Item item = Jenkins.get().getItemByFullName(config.getJobName());
            if (item == null) {
                logger.debug(job.getFullName() + "' contains phase job  '" + config.getJobName() + "' that is not found.");
            } else if (item instanceof AbstractProject) {
                tmpProject = (AbstractProject) item;
                if (processedJobs.contains(tmpProject)) {
                    logger.warn(String.format("encountered circular reference from %s to %s", job.getFullName(), tmpProject.getFullName()));
                } else {
                    items.add(tmpProject);
                }
            } else {
                logger.warn(job.getFullName() + "' contains phase job '" + config.getJobName() + "' that is not AbstractProject");
            }
        }
        super.phases.add(ModelFactory.createStructurePhase(b.getPhaseName(), true, items, processedJobs));
    }
}
