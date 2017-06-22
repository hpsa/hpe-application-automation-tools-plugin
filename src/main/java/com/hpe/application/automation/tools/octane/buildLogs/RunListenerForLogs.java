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

package com.hpe.application.automation.tools.octane.buildLogs;

import com.google.inject.Inject;
import com.hp.mqm.client.MqmRestClient;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactoryImpl;
import com.hpe.application.automation.tools.octane.configuration.BdiConfiguration;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hpe.application.automation.tools.octane.configuration.ServerConfiguration;
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
 * Created by benmeior on 11/16/2016
 */

@Extension
public class RunListenerForLogs extends RunListener<Run> {
	private static Logger logger = LogManager.getLogger(RunListenerForLogs.class);

	private JenkinsMqmRestClientFactory clientFactory;

	@Inject
	private LogDispatcher logDispatcher;
	@Inject
	private BdiConfigurationFetcher bdiConfigurationFetcher;

	//  [YG] TODO: move workspace resolving logic to the async processor off the main thread
	@Override
	public void onCompleted(Run r, @Nonnull TaskListener listener) {
		BdiConfiguration bdiConfiguration = bdiConfigurationFetcher.obtain();
		if (bdiConfiguration == null || !bdiConfiguration.isFullyConfigured()) {
			logger.debug("BDI is not configured in Octane");
			return;
		}

		if (!(r instanceof AbstractBuild)) {
			return;
		}

		AbstractBuild build = (AbstractBuild) r;

		try {
			MqmRestClient mqmRestClient = createMqmRestClient();
			if (mqmRestClient == null) {
				logger.warn("Octane configuration is not valid");
				return;
			}

			List<String> workspaces = mqmRestClient.getJobWorkspaceId(ConfigurationService.getModel().getIdentity(), build.getParent().getName());
			if (workspaces.isEmpty()) {
				logger.info(String.format("Job '%s' is not part of an Octane pipeline in any workspace, so its log will not be sent.", build.getParent().getName()));
			} else {
				for (String workspace : workspaces) {
					logger.info(String.format("Enqueued job [%s#%d] of workspace %s", build.getParent().getName(), build.getNumber(), workspace));
					logDispatcher.enqueueLog(build.getProject().getName(), build.getNumber(), workspace);
				}
			}
		} catch (Exception e) {
			logger.error(String.format("Could not enqueue log for job %s", build.getParent().getName()));
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

	@Inject
	public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
		this.clientFactory = clientFactory;
	}
}
