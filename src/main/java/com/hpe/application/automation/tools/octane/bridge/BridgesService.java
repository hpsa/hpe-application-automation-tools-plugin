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

package com.hpe.application.automation.tools.octane.bridge;

import com.google.inject.Inject;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactoryImpl;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationListener;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hpe.application.automation.tools.octane.configuration.ServerConfiguration;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by gullery on 05/08/2015.
 * <p>
 * Bridge Service meant to provide an abridged connectivity functionality
 * The only APIs to be exposed is the basic management of abridged clients
 */

@Extension
public class BridgesService implements ConfigurationListener {
	private static final Logger logger = LogManager.getLogger(BridgesService.class);

	private static BridgesService extensionInstance;
	private JenkinsMqmRestClientFactory clientFactory;
	private BridgeClient bridgeClient;

	public static BridgesService getExtensionInstance() {
		if (extensionInstance == null) {
			List<BridgesService> extensions = Jenkins.getInstance().getExtensionList(BridgesService.class);
			if (extensions.isEmpty()) {
				throw new RuntimeException("bridge service was not initialized properly");
			} else if (extensions.size() > 1) {
				throw new RuntimeException("bridge service expected to be singleton, found " + extensions.size() + " instances");
			} else {
				extensionInstance = extensions.get(0);
			}
		}
		return extensionInstance;
	}

	public void updateBridge(ServerConfiguration conf, String serverIdentity) {
		if (conf.isValid()) {
			if (bridgeClient != null) {
				bridgeClient.update(conf, serverIdentity);
			} else {
				bridgeClient = new BridgeClient(conf, clientFactory, serverIdentity);
			}
		} else {
			if (bridgeClient != null) {
				logger.info("empty / non-valid configuration submitted, disposing bridge client");
				bridgeClient.dispose();
				bridgeClient = null;
			}
		}
	}

	@Inject
	public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
		updateBridge(conf, ConfigurationService.getModel().getIdentity());
	}
}
