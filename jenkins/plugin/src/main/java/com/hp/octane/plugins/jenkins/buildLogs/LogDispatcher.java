package com.hp.octane.plugins.jenkins.buildLogs;

import com.google.inject.Inject;
import com.hp.indi.bdi.client.BdiClient;
import com.hp.indi.bdi.client.BdiClientFactory;
import com.hp.indi.bdi.client.BdiConstants;
import com.hp.octane.plugins.jenkins.ResultQueue;
import com.hp.octane.plugins.jenkins.client.RetryModel;
import com.hp.octane.plugins.jenkins.configuration.BdiConfiguration;
import com.hp.octane.plugins.jenkins.identity.ServerIdentity;
import com.hp.octane.plugins.jenkins.tests.SafeLoggingAsyncPeriodWork;
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
public class LogDispatcher extends SafeLoggingAsyncPeriodWork {
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

        BdiClient client = BdiClientFactory.getBdiClient(configuration.getHost(), Integer.parseInt(configuration.getPort()), true);

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
                response = client.post(BdiConstants.CONSOLE_LOG_DATA_TYPE, BDI_PRODUCT, Long.valueOf(configuration.getTenantId()),
                        item.getWorkspace(), buildDataId(build), build.getLogFile());

                // OBM: After response for data-in is changed, validate it

                logger.info("Successfully sent log of build [" + item.getProjectName() + "#" + item.getBuildNumber()
                        + "]. response from bdi server: " + response);

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
        String ciServerId = ServerIdentity.getIdentity();
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
    public void setLogResultQueue(LogResultQueue queue) {
        this.logsQueue = queue;
    }
}
