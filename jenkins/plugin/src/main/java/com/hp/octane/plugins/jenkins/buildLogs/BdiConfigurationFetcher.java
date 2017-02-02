package com.hp.octane.plugins.jenkins.buildLogs;

import com.google.inject.Inject;
import com.hp.mqm.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.octane.plugins.jenkins.configuration.BdiConfiguration;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.tests.SafeLoggingAsyncPeriodWork;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.util.TimeUnit2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by benmeior on 12/21/2016.
 */
@Extension
public class BdiConfigurationFetcher extends SafeLoggingAsyncPeriodWork {

    private static Logger logger = LogManager.getLogger(BdiConfigurationFetcher.class);

    private JenkinsMqmRestClientFactory clientFactory;

    private static BdiConfiguration bdiConfiguration;

    private boolean shouldFetchBdiConfiguration;

    public BdiConfigurationFetcher() {
        super("BDI configuration fetcher");
        shouldFetchBdiConfiguration = true;
    }

    public synchronized BdiConfiguration obtain() {
        if (shouldFetchBdiConfiguration && bdiConfiguration == null) {
            fetchBdiConfiguration();
        }
        return bdiConfiguration;
    }

    public void refresh() {
        fetchBdiConfiguration();
    }

    private void fetchBdiConfiguration() {
        try {
            MqmRestClient mqmRestClient = createMqmRestClient();
            bdiConfiguration = BdiConfiguration.fromJSON(mqmRestClient.getBdiConfiguration());
            shouldFetchBdiConfiguration = false;

            String loggingMessage = bdiConfiguration == null ? "BDI is not configured in Octane." : "Fetched BDI configuration from Octane. Address: " + bdiConfiguration.getHost() + ", Port: " + bdiConfiguration.getPort();
            logger.info(loggingMessage);
        } catch (Exception e) {
            logger.error("Failed to fetch BDI configuration from Octane", e);
        }
    }

    private MqmRestClient createMqmRestClient() {
        ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
        return clientFactory.obtain(
                configuration.location,
                configuration.sharedSpace,
                configuration.username,
                configuration.password);
    }

    @Override
    protected void doExecute(TaskListener listener) throws IOException, InterruptedException {
        fetchBdiConfiguration();
    }

    @Override
    public long getRecurrencePeriod() {
        return TimeUnit2.DAYS.toMillis(1);
    }

    @Inject
    public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;
    }
}
