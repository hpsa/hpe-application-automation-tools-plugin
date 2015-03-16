// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.client.MqmRestClientImpl;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import hudson.util.TimeUnit2;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class TestDispatcher extends AsyncPeriodicWork {

    private static Logger logger = Logger.getLogger(TestDispatcher.class.getName());

    private static final String TEST_RESULT_PUSH_ENDPOINT = "/tb/build-push";

    private static final LinkedList<AbstractBuild> queue = new LinkedList<AbstractBuild>();

    @Inject
    private LockoutModel lockoutModel;

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
        if (lockoutModel.isQuietPeriod()) {
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
            lockoutModel.failure();
            return;
        }
        if (!client.createSession()) {
            logger.warning("Could not create session, pending test results can't be submitted");
            lockoutModel.failure();
            return;
        }
        lockoutModel.success();

        while (true) {
            AbstractBuild build;
            synchronized (TestDispatcher.class) {
                if (queue.isEmpty()) {
                    break;
                }
                build = queue.removeFirst();
            }
            if (!pushTestResults(client, build)) {
                // TODO: janotav: try again later
            }
        }
    }

    private boolean pushTestResults(MqmRestClient client, AbstractBuild build) {
        File resultFile = new File(build.getRootDir(), TestListener.TEST_RESULT_FILE);
        try {
            client.post(TEST_RESULT_PUSH_ENDPOINT, resultFile, "application/xml");
        } catch (IOException e) {
            logger.log(Level.WARNING, "There are pending test results, connecting to the MQM server", e);
            return false;
        }
        return true;
    }

    @Override
    public long getRecurrencePeriod() {
        return TimeUnit2.SECONDS.toMillis(10);
    }

    public synchronized static void add(AbstractBuild build) {
        queue.add(build);
    }
}
