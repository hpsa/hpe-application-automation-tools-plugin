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

package com.hp.application.automation.tools.octane.executor;

import com.hp.application.automation.tools.octane.tests.AbstractSafeLoggingAsyncPeriodWork;
import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PeriodicWork;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.List;


/**
 * This class cleans outdated test execution jobs that were not used more than 7 days.
 * Only jobs that were created by {@link TestExecutionJobCreatorService} are checked.
 */
@Extension
public class UftJobCleaner extends AbstractSafeLoggingAsyncPeriodWork {

    private static Logger logger = LogManager.getLogger(UftJobCleaner.class);


    public UftJobCleaner() {
        super("Uft Job Cleaner");
        logger.warn(String.format("Initial delay %d minutes, recurrencePeriod %d minutes, outdate threshold %d days", getInitialDelay() / MIN, getRecurrencePeriod() / MIN, getOutdateThreshold()));
    }

    @Override
    public long getRecurrencePeriod() {
        return HOUR;
    }

    @Override
    public long getInitialDelay() {
        return MIN * 10;//start 10 minutes after jenkins restart
    }

    private long getOutdateThreshold() {
        int DEFAULT_OUTDATE_THRESHOLD = 7;
        int threshold = DEFAULT_OUTDATE_THRESHOLD;
        String paramValue = System.getProperty("octane.plugin.UftJobCleaner.outdateThreshold");
        if (StringUtils.isNotEmpty(paramValue)) {
            try {
                threshold = Integer.parseInt(paramValue);
            } catch (NumberFormatException e) {
                threshold = DEFAULT_OUTDATE_THRESHOLD;
            }
        }
        return threshold;
    }

    @Override
    protected void doExecute(TaskListener listener) throws IOException, InterruptedException {
        List<FreeStyleProject> jobs = Jenkins.getInstance().getAllItems(FreeStyleProject.class);
        long thresholdTimeInMillis = new Date().getTime() - PeriodicWork.DAY * getOutdateThreshold();

        int clearCounter = 0;
        for (FreeStyleProject job : jobs) {
            if (isExecutorJob(job) && job.getLastBuild() != null && !job.isBuilding()) {
                if (thresholdTimeInMillis > job.getLastBuild().getTimeInMillis()) {
                    try {
                        logger.warn(String.format("Job %s is going to be deleted as outdated job, last build was executed at %s", job.getName(), job.getLastBuild().getTimestampString2()));
                        job.delete();
                    } catch (Exception e) {
                        logger.warn(String.format("Failed to delete job %s : %s", job.getName(), e.getMessage()));
                    }

                    clearCounter++;
                }
            }
        }

        logger.warn(String.format("Cleaner found %s outdated job", clearCounter));
    }

    private boolean isExecutorJob(FreeStyleProject job) {
        ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
        if (parameters != null &&
                parameters.getParameterDefinition(TestExecutionJobCreatorService.SUITE_ID_PARAMETER_NAME) != null &&
                parameters.getParameterDefinition(TestExecutionJobCreatorService.SUITE_RUN_ID_PARAMETER_NAME) != null) {
            return true;
        }
        return false;
    }


}
