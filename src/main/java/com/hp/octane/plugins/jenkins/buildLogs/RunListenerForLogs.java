package com.hp.octane.plugins.jenkins.buildLogs;

import com.google.inject.Inject;
import com.hp.mqm.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.octane.plugins.jenkins.configuration.BdiConfiguration;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
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
