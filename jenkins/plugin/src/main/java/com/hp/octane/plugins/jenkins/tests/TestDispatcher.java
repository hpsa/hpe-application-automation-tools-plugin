// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.google.inject.Inject;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.DomainProjectNotExistException;
import com.hp.mqm.client.exception.LoginException;
import com.hp.mqm.client.exception.FileNotFoundException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.octane.plugins.jenkins.client.RetryModel;
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

    @Inject
    private RetryModel retryModel;

    private TestResultQueue queue;

    private JenkinsMqmRestClientFactory clientFactory;

    public TestDispatcher() {
        super("MQM Test Dispatcher");
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        if (queue.isEmpty()) {
            return;
        }
        if (retryModel.isQuietPeriod()) {
            logger.info("There are pending test results, but we are in quiet period");
            return;
        }

        ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
        if (StringUtils.isEmpty(configuration.location)) {
            logger.warning("There are pending test results, but MQM server location is not specified, results can't be submitted");
            return;
        }

        logger.info("There are pending test results, connecting to the MQM server");
        MqmRestClient client = clientFactory.create(
                configuration.location,
                configuration.domain,
                configuration.project,
                configuration.username,
                configuration.password);

        try {
            client.tryToConnectProject();
        } catch (DomainProjectNotExistException e) {
            logger.log(Level.WARNING, "Invalid domain or project. Pending test results can't be submitted", e);
            retryModel.failure();
            return;
        } catch (LoginException e) {
            logger.log(Level.WARNING, "Login failed, pending test results can't be submitted", e);
            retryModel.failure();
            return;
        } catch (RequestException e) {
            logger.log(Level.WARNING, "Problem with communication with MQM server. Pending test results can't be submitted", e);
            retryModel.failure();
            return;
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Connection problem, pending test results can't be submitted", e);
            retryModel.failure();
            return;
        }

        retryModel.success();

        while (!queue.isEmpty()) {
            TestResultQueue.QueueItem item = queue.removeFirst();
            AbstractProject project = (AbstractProject) Jenkins.getInstance().getItem(item.projectName);
            if (project == null) {
                logger.warning("Project [" + item.projectName + "] no longer exists, pending test results can't be submitted");
                return;
            }

            AbstractBuild build = project.getBuildByNumber(item.buildNumber);
            if (build == null) {
                logger.warning("Build [" + item.projectName + "#" + item.buildNumber + "] no longer exists, pending test results can't be submitted");
                return;
            }
            try {
                if (!pushTestResults(client, build)) {
                    // TODO: janotav: try again later
                }
            } catch (FileNotFoundException e) {
                logger.log(Level.WARNING, "Failed to submit test results [" + build.getProject().getName() + "#" + build.getNumber() + "]. Test result file not found.", e);
            }
        }

    }

    private boolean pushTestResults(MqmRestClient client, AbstractBuild build) {
        File resultFile = new File(build.getRootDir(), TestListener.TEST_RESULT_FILE);
        try {
            client.postTestResult(resultFile);
        } catch (RequestException e) {
            logger.log(Level.WARNING, "Failed to submit test results [" + build.getProject().getName() + "#" + build.getNumber() + "]", e);
            return false;
        } catch (RequestErrorException e) {
            logger.log(Level.WARNING, "Failed to submit test results [" + build.getProject().getName() + "#" + build.getNumber() + "]", e);
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
    public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Inject
    public void setTestResultQueue(TestResultQueueImpl queue) {
        this.queue = queue;
    }

    /*
     * To be used in tests only.
     */
    public void _setMqmRestClientFactory(JenkinsMqmRestClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /*
     * To be used in tests only.
     */
    public void _setTestResultQueue(TestResultQueue queue) {
        this.queue = queue;
    }
}
