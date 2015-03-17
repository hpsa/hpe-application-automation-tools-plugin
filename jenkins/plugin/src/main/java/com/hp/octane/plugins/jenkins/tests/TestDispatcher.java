// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.client.RetryModel;
import com.hp.octane.plugins.jenkins.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.client.MqmRestClientImpl;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import hudson.util.TimeUnit2;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class TestDispatcher extends AsyncPeriodicWork {

    private static Logger logger = Logger.getLogger(TestDispatcher.class.getName());

    private static final String TEST_RESULT_PUSH_ENDPOINT = "/tb/build-push";

    // TODO: janotav: use persistent queue (square:tape?)
    private static final LinkedList<QueueItem> queue = new LinkedList<QueueItem>();

    @Inject
    private RetryModel retryModel;

    public TestDispatcher() {
        super("MQM Test Dispatcher");
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        synchronized (TestDispatcher.class) {
            if (queue.isEmpty()) {
                return;
            }
        }
        if (retryModel.isQuietPeriod()) {
            logger.info("There are pending test results, but we are in quiet period");
            return;
        }

        logger.info("There are pending test results, connecting to the MQM server");
        ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
        MqmRestClient client = new MqmRestClientImpl(
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

        while (true) {
            QueueItem item;
            synchronized (TestDispatcher.class) {
                if (queue.isEmpty()) {
                    break;
                }
                item = queue.removeFirst();
            }
            AbstractProject project = (AbstractProject) Jenkins.getInstance().getItem(item.projectName);
            if (project == null) {
                logger.warning("Project [" +  item.projectName + "] no longer exists, pending test results can't be submitted");
                return;
            }

            AbstractBuild build = project.getBuildByNumber(item.buildNumber);
            if (build == null) {
                logger.warning("Build [" + item.projectName + "#" + item.buildNumber  + "] no longer exists, pending test results can't be submitted");
                return;
            }

            if (!pushTestResults(client, build)) {
                // TODO: janotav: try again later
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
        return TimeUnit2.SECONDS.toMillis(10);
    }

    public synchronized static void add(AbstractBuild build) {
        queue.add(new QueueItem(build.getProject().getName(), build.getNumber()));
    }

    private static class QueueItem implements Serializable {
        String projectName;
        int buildNumber;

        QueueItem(String projectName, int buildNumber) {
            this.projectName = projectName;
            this.buildNumber = buildNumber;
        }
    }
}
