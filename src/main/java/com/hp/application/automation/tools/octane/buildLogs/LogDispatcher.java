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

package com.hp.application.automation.tools.octane.buildLogs;

import com.google.inject.Inject;
import com.hp.indi.bdi.client.BdiClient;
import com.hp.indi.bdi.client.BdiClientFactory;
import com.hp.application.automation.tools.octane.ResultQueue;
import com.hp.application.automation.tools.octane.client.RetryModel;
import com.hp.application.automation.tools.octane.configuration.BdiConfiguration;
import com.hp.application.automation.tools.octane.configuration.ConfigurationService;
import com.hp.application.automation.tools.octane.tests.AbstractSafeLoggingAsyncPeriodWork;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.TimeUnit2;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by benmeior on 11/20/2016.
 */
@Extension
public class LogDispatcher extends AbstractSafeLoggingAsyncPeriodWork {
    private static final String BDI_PRODUCT = "octane";
    private static Logger logger = LogManager.getLogger(LogDispatcher.class);

    @Inject
    private RetryModel retryModel;

    @Inject
    private BdiConfigurationFetcher bdiConfigurationFetcher;

    private ResultQueue logsQueue;

    public LogDispatcher() {
        super("BDI log dispatcher");
    }

    @Override
    protected void doExecute(TaskListener listener) throws IOException, InterruptedException {
        if (logsQueue.peekFirst() == null) {
            return;
        }
        if (retryModel.isQuietPeriod()) {
            logger.info("There are pending logs, but we are in quiet period");
            return;
        }
        manageLogsQueue();
    }

    private void manageLogsQueue() {
        BdiConfiguration configuration = bdiConfigurationFetcher.obtain();
        if (configuration == null || !configuration.isFullyConfigured()) {
            logger.error("Could not send logs. BDI is not configured");
            return;
        }

        BdiClient client = BdiClientFactory.getBdiClient(configuration.getHost(), Integer.parseInt(configuration.getPort()));

        // Configure proxy if needed
        ProxyConfiguration proxy = Jenkins.getInstance().proxy;
        if (proxy != null) {
            client.setProxy(proxy.name, proxy.port);
        }

        String response;
        ResultQueue.QueueItem item;
        while ((item = logsQueue.peekFirst()) != null) {
            Run build = getBuildFromQueueItem(item);
            if (build == null) {
                logsQueue.remove();
                continue;
            }
            try {
                //TODO: fix bdi!
//                client.post("consolelog",BDI_PRODUCT,Long.valueOf(configuration.getTenantId()),
//                        item.getWorkspace(),buildDataId(build),build.getLogFile());
//                client.post("consolelog",BDI_PRODUCT,Long.valueOf(configuration.getTenantId()),
//                        item.getWorkspace(),buildDataId(build),build.getLogFile());
////                response = client.post("consolelog", BDI_PRODUCT, Long.valueOf(configuration.getTenantId()),
//                        item.getWorkspace(), buildDataId(build), build.getLogFile());

                // OBM: After response for data-in is changed, validate it

//                logger.info("Successfully sent log of build [" + item.getProjectName() + "#" + item.getBuildNumber()
//                        + "]. response from bdi server: " + response);

                logsQueue.remove();
            } catch (Exception e) {
                logger.error(String.format("Could not send log of build [%s#%s] to bdi.", item.getProjectName(), item.getBuildNumber()), e);
                if (!logsQueue.failed()) {
                    logger.warn("Maximum number of attempts reached, operation will not be re-attempted for this build");
                }
            }
        }
    }

    private Run getBuildFromQueueItem(ResultQueue.QueueItem item) {
        Job project = (Job) Jenkins.getInstance().getItemByFullName(item.getProjectName());
        if (project == null) {
            logger.warn("Project [" + item.getProjectName() + "] no longer exists, pending logs can't be submitted");
            return null;
        }

        Run build = project.getBuildByNumber(item.getBuildNumber());
        if (build == null) {
            logger.warn("Build [" + item.getProjectName() + "#" + item.getBuildNumber() + "] no longer exists, pending logs can't be submitted");
            return null;
        }
        return build;
    }

    private String buildDataId(Run build) {
        String ciServerId = ConfigurationService.getModel().getIdentity();
        String ciBuildId = String.valueOf(build.getNumber());
        String jobName = build.getParent().getName();

        return String.format("%s-%s-%s", ciServerId, ciBuildId, jobName.replaceAll(" ", ""));
    }

    @Override
    public long getRecurrencePeriod() {
        String value = System.getProperty("BDI.LogDispatcher.Period"); // let's us config the recurrence period. default is 10 seconds.
        if (!StringUtils.isEmpty(value)) {
            return Long.valueOf(value);
        }
        return TimeUnit2.SECONDS.toMillis(10);
    }

    void enqueueLog(String projectName, int buildNumber, String workspace) {
        logsQueue.add(projectName, buildNumber, workspace);
    }

    @Inject
    public void setLogResultQueue(LogAbstractResultQueue queue) {
        this.logsQueue = queue;
    }
}
