// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.client.MqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.MqmRestClientFactoryImpl;
import com.hp.octane.plugins.jenkins.client.RetryModel;
import com.hp.octane.plugins.jenkins.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import hudson.util.TimeUnit2;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class TestDispatcher extends AsyncPeriodicWork {

    private static Logger logger = Logger.getLogger(TestDispatcher.class.getName());

    private static final String TEST_RESULT_PUSH_ENDPOINT = "/test-results/v1";

    @Inject
    private RetryModel retryModel;

    private TestResultQueue queue;

    private MqmRestClientFactory clientFactory;

    public TestDispatcher() {
        super("MQM Test Dispatcher");
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        if (queue.peekFirst() == null) {
            return;
        }
        if (retryModel.isQuietPeriod()) {
            logger.info("There are pending test results, but we are in quiet period");
            return;
        }
        MqmRestClient client = null;
        TestResultQueue.QueueItem item;
        while ((item = queue.peekFirst()) != null) {
            if (client == null) {
                ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
                if (StringUtils.isEmpty(configuration.location)) {
                    logger.warning("There are pending test results, but MQM server location is not specified, results can't be submitted");
                    return;
                }

                logger.info("There are pending test results, connecting to the MQM server");
                client = clientFactory.create(
                        configuration.location,
                        configuration.domain,
                        configuration.project,
                        configuration.username,
                        configuration.password);
                if (!client.login()) {
                    logger.warning("Could not authenticate, pending test results can't be submitted");
                    retryModel.failure();
                    return;
                }
                if (!client.createSession()) {
                    logger.warning("Could not create session, pending test results can't be submitted");
                    retryModel.failure();
                    return;
                }
                if (!client.checkDomainAndProject()) {
                    logger.warning("Could not validate domain and project, pending test results can't be submitted");
                    retryModel.failure();
                    return;
                }
                retryModel.success();
            }

            AbstractProject project = (AbstractProject) Jenkins.getInstance().getItem(item.projectName);
            if (project == null) {
                logger.warning("Project [" + item.projectName + "] no longer exists, pending test results can't be submitted");
                queue.remove();
                continue;
            }

            AbstractBuild build = project.getBuildByNumber(item.buildNumber);
            if (build == null) {
                logger.warning("Build [" + item.projectName + "#" + item.buildNumber + "] no longer exists, pending test results can't be submitted");
                queue.remove();
                continue;
            }

            if (pushTestResults(client, build)) {
                logger.info("Successfully pushed test results of build [" + item.projectName + "#" + item.buildNumber + "]");
                queue.remove();
            } else {
                logger.warning("Failed to push test results of build [" + item.projectName + "#" + item.buildNumber + "]");
                if (!queue.failed()) {
                    logger.warning("Maximum number of attempts reached, operation will not be re-attempted for this build");
                }
                client = null;
            }
        }
    }

    private boolean pushTestResults(MqmRestClient client, AbstractBuild build) {
        File resultFile = new File(build.getRootDir(), TestListener.TEST_RESULT_FILE);
        try {
            int code = client.post(TEST_RESULT_PUSH_ENDPOINT, resultFile, "application/xml");
            if (code != 201) {
                logger.log(Level.WARNING, "Failed to submit test results [" + build.getProject().getName() + "#" + build.getNumber()  + "]: code=" + code);
                return false;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to submit test results [" + build.getProject().getName() + "#" + build.getNumber()  + "]", e);
            return false;
        }
        return true;
    }

    @Override
    public long getRecurrencePeriod() {
        String value = System.getProperty("MQM.TestDispatcher.Period");
        if (!StringUtils.isEmpty(value)) {
            return Long.valueOf(value);
        }
        return TimeUnit2.SECONDS.toMillis(10);
    }

    @Inject
    public void setMqmRestClientFactory(MqmRestClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Inject
    public void setTestResultQueue(TestResultQueueImpl queue) {
        this.queue = queue;
    }

    /*
     * To be used in tests only.
     */
    public void _setMqmRestClientFactory(MqmRestClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /*
     * To be used in tests only.
     */
    public void _setTestResultQueue(TestResultQueue queue) {
        this.queue = queue;
    }
}
