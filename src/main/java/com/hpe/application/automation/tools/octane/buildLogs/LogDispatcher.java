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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.hp.indi.bdi.client.BdiClientV2;
import com.hp.mqm.client.MqmRestClient;
import com.hpe.application.automation.tools.octane.ResultQueue;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactoryImpl;
import com.hpe.application.automation.tools.octane.client.RetryModel;
import com.hpe.application.automation.tools.octane.configuration.BdiConfiguration;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hpe.application.automation.tools.octane.configuration.ServerConfiguration;
import com.hpe.application.automation.tools.octane.tests.AbstractSafeLoggingAsyncPeriodWork;
import com.hp.indi.bdi.client.BdiClient;
import com.hp.indi.bdi.client.BdiClientFactory;
import com.hp.indi.bdi.client.BdiProxyConfiguration;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.console.PlainTextConsoleOutputStream;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.TimeUnit2;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Created by benmeior on 11/20/2016
 */

@Extension
public class LogDispatcher extends AbstractSafeLoggingAsyncPeriodWork {
	private static final Logger logger = LogManager.getLogger(LogDispatcher.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final String OCTANE_LOG_FILE_NAME = "octane_log";
	private static final String BDI_PRODUCT = "octane";
	private static final String CONSOLE_LOG_DATA_TYPE = "consolelog";

	@Inject
	private RetryModel retryModel;
	@Inject
	private BdiConfigurationFetcher bdiConfigurationFetcher;

	private JenkinsMqmRestClientFactory clientFactory;
	private ResultQueue logsQueue;
	private BdiClientV2 bdiClient;
	private BdiClient deprecatedClient;
	private ProxyConfiguration proxyConfiguration;

	public LogDispatcher() {
		super("BDI log dispatcher");
	}

	private void initClient() {
		closeClient();

		this.proxyConfiguration = Jenkins.getInstance().proxy;

		BdiConfiguration bdiConfiguration = bdiConfigurationFetcher.obtain();
		if (bdiConfiguration == null || !bdiConfiguration.isFullyConfigured()) {
			logger.debug("BDI is not configured in Octane");
			return;
		}

		if (proxyConfiguration == null) {
			bdiClient = BdiClientFactory.getBdiClientV2(bdiConfiguration.getHost(), bdiConfiguration.getPort());
			deprecatedClient = BdiClientFactory.getBdiClient(bdiConfiguration.getHost(), bdiConfiguration.getPort());
		} else {
			BdiProxyConfiguration bdiProxyConfiguration = new BdiProxyConfiguration(proxyConfiguration.name, proxyConfiguration.port, proxyConfiguration.getUserName(), proxyConfiguration.getPassword());
			bdiClient = BdiClientFactory.getBdiClientV2(bdiConfiguration.getHost(), bdiConfiguration.getPort(), bdiProxyConfiguration);
			deprecatedClient = BdiClientFactory.getBdiClient(bdiConfiguration.getHost(), bdiConfiguration.getPort(), bdiProxyConfiguration);
		}
	}

	@Override
	protected void doExecute(TaskListener listener) {
		if (logsQueue.peekFirst() == null) {
			return;
		}
		if (retryModel.isQuietPeriod()) {
			logger.info("There are pending logs, but we are in quiet period");
			return;
		}

		//  verify configuration
		BdiConfiguration bdiConfiguration = bdiConfigurationFetcher.obtain();
		if (bdiConfiguration == null || !bdiConfiguration.isFullyConfigured()) {
			logger.error("Could not send logs. BDI is not configured");
			logsQueue.clear();
			closeClient();
			return;
		} else if (!bdiConfiguration.isAccessTokenFlavor() && !isPemFilePropertyInit()) {
			return;
		}

		//  proceed to process queue item
		manageLogsQueue(bdiConfiguration);
	}

	private void manageLogsQueue(BdiConfiguration bdiConfiguration) {
		ResultQueue.QueueItem item;
		File logFile = null;
		Run build = null;
		while ((item = logsQueue.peekFirst()) != null) {
			try {
				build = getBuildFromQueueItem(item);
				if (build == null) {
					logsQueue.remove();
					continue;
				}

				logFile = getOctaneLogFile(build);

				if (bdiClient == null || deprecatedClient == null || proxyConfiguration != Jenkins.getInstance().proxy) {
					initClient();
				}

				if (bdiConfiguration.isAccessTokenFlavor()) {
					bdiClient.post(CONSOLE_LOG_DATA_TYPE, BDI_PRODUCT, bdiConfiguration.getTenantId(), item.getWorkspace(), buildDataId(build), logFile, retrieveAccessToken());
				} else {
					deprecatedClient.post(CONSOLE_LOG_DATA_TYPE, BDI_PRODUCT, bdiConfiguration.getTenantId(), item.getWorkspace(), buildDataId(build), logFile);
				}

				logger.info(String.format("Successfully sent log of build [%s#%s]", item.getProjectName(), item.getBuildNumber()));

				logsQueue.remove();
				Files.deleteIfExists(logFile.toPath());
			} catch (Exception e) {
				logger.error(String.format("Could not send log of build [%s#%s] to bdi.", item.getProjectName(), item.getBuildNumber()), e);
				if (!logsQueue.failed()) {
					logger.warn("Maximum number of attempts reached, operation will not be re-attempted for this build");
					if (logFile != null) {
						try {
							Files.deleteIfExists(logFile.toPath());
						} catch (IOException e1) {
							String errorMsg = "Could not delete Octane log file";
							if (build != null) {
								errorMsg = String.format("%s of %s#%s", errorMsg, build.getParent().getName(), String.valueOf(build.getNumber()));
							}
							logger.error(errorMsg);
						}
					}
				}
			}
		}
	}

	private File getOctaneLogFile(Run build) throws IOException {
		String octaneLogFilePath = build.getLogFile().getParent() + File.separator + OCTANE_LOG_FILE_NAME;
		File logFile = new File(octaneLogFilePath);
		if (!logFile.exists()) {
			try (FileOutputStream fileOutputStream = new FileOutputStream(logFile);
			     InputStream logStream = build.getLogInputStream();
			     PlainTextConsoleOutputStream out = new PlainTextConsoleOutputStream(fileOutputStream)) {
				IOUtils.copy(logStream, out);
				out.flush();
			}
		}
		return logFile;
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

	private String retrieveAccessToken() throws IOException {
		String result;
		MqmRestClient mqmRestClient = createMqmRestClient();
		if (mqmRestClient != null) {
			BdiTokenData bdiTokenData = objectMapper.readValue(mqmRestClient.getBdiTokenData(), BdiTokenData.class);
			if (bdiTokenData.token != null && !bdiTokenData.token.isEmpty()) {
				result = bdiTokenData.token;
			} else {
				throw new IllegalStateException("invalid access token received from Octane: " + bdiTokenData.token);
			}
		} else {
			throw new IllegalStateException("failed to create RestClient to retrieve access token from Octane");
		}
		return result;
	}

	private void closeClient() {
		try {
			if (bdiClient != null) bdiClient.close();
			if (deprecatedClient != null) deprecatedClient.close();
		} catch (Exception e) {
			logger.error("Failed to close BDI client");
		} finally {
			bdiClient = null;
			deprecatedClient = null;
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
		String ciServerId = ConfigurationService.getModel().getIdentity();
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
	public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Inject
	public void setLogResultQueue(LogAbstractResultQueue queue) {
		this.logsQueue = queue;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	private static final class BdiTokenData {
		private String token;
	}

	private boolean isPemFilePropertyInit() {
		return System.getProperty("pem_file") != null && !System.getProperty("pem_file").isEmpty();
	}
}
