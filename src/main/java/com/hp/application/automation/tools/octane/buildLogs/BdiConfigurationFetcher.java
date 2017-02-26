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
import com.hp.mqm.client.MqmRestClient;
import com.hp.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hp.application.automation.tools.octane.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.application.automation.tools.octane.configuration.BdiConfiguration;
import com.hp.application.automation.tools.octane.configuration.ConfigurationService;
import com.hp.application.automation.tools.octane.configuration.ServerConfiguration;
import com.hp.application.automation.tools.octane.tests.AbstractSafeLoggingAsyncPeriodWork;
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
public class BdiConfigurationFetcher extends AbstractSafeLoggingAsyncPeriodWork {

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
            shouldFetchBdiConfiguration = false;
            MqmRestClient mqmRestClient = createMqmRestClient();
            if (mqmRestClient == null) {
                logger.info("Octane configuration is not valid");
                bdiConfiguration = null;
                return;
            }
            bdiConfiguration = BdiConfiguration.fromJSON(mqmRestClient.getBdiConfiguration());

            String loggingMessage = bdiConfiguration == null ? "BDI is not configured in Octane." : "Fetched BDI configuration from Octane. Address: " + bdiConfiguration.getHost() + ", Port: " + bdiConfiguration.getPort();
            logger.info(loggingMessage);
        } catch (Exception e) {
            logger.error("Failed to fetch BDI configuration from Octane", e);
        }
    }

    private MqmRestClient createMqmRestClient() {
        ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
        if (configuration.isValid()) {
            return clientFactory.obtain(
                    configuration.location,
                    configuration.sharedSpace,
                    configuration.username,
                    configuration.password);
        }
        return null;
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
