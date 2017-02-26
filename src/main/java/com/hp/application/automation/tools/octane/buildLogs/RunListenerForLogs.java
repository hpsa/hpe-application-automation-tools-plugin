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
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by benmeior on 11/16/2016.
 */
@Extension
public class RunListenerForLogs extends RunListener<Run> {
    public RunListenerForLogs(){}

    private static Logger logger = LogManager.getLogger(RunListenerForLogs.class);

    private JenkinsMqmRestClientFactory clientFactory;

    @Inject
    private LogDispatcher logDispatcher;

    @Inject
    private BdiConfigurationFetcher bdiConfigurationFetcher;

    @Override
    public void onCompleted(Run r, @Nonnull TaskListener listener) {
        BdiConfiguration bdiConfiguration = bdiConfigurationFetcher.obtain();
        if (bdiConfiguration == null || !bdiConfiguration.isFullyConfigured()) {
            return;
        }

        if (!(r instanceof AbstractBuild)) {
            return;
        }

        AbstractBuild build = (AbstractBuild) r;

        MqmRestClient mqmRestClient = createMqmRestClient();
        List<String> workspaces = mqmRestClient.getJobWorkspaceId(ConfigurationService.getModel().getIdentity(), build.getParent().getName());
        if (workspaces.isEmpty()) {
            logger.info(String.format("Job '%s' is not part of an Octane pipeline in any workspace, so its log will not be sent.", build.getParent().getName()));
        } else {
            for (String workspace : workspaces) {
                logDispatcher.enqueueLog(build.getProject().getName(), build.getNumber(), workspace);
            }
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

    @Inject
    public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;
    }
}
