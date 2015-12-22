package com.hp.octane.plugins.jenkins.bridge;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationListener;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;
import jenkins.model.Jenkins;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gullery on 05/08/2015.
 * <p/>
 * Bridge Service meant to provide an abridged connectivity functionality
 * The only APIs to be exposed is the basic management of abridged clients
 */

@Extension
public class BridgesService implements ConfigurationListener {
	private static final Logger logger = Logger.getLogger(BridgesService.class.getName());

	private static BridgesService extensionInstance;
	private JenkinsMqmRestClientFactory clientFactory;
	private BridgeClient bridgeClient;

	public static BridgesService getExtensionInstance() {
		if (extensionInstance == null) {
			List<BridgesService> extensions = Jenkins.getInstance().getExtensionList(BridgesService.class);
			if (extensions.isEmpty()) {
				throw new RuntimeException("BRIDGE: bridge service was not initialized properly");
			} else if (extensions.size() > 1) {
				throw new RuntimeException("BRIDGE: bridge service expected to be singleton, found " + extensions.size() + " instances");
			} else {
				extensionInstance = extensions.get(0);
			}
		}
		return extensionInstance;
	}

	public void updateBridge(ServerConfiguration conf) {
		if (isConfigurationValid(conf)) {
			if (bridgeClient != null) {
				bridgeClient.update(conf);
			} else {
				bridgeClient = new BridgeClient(conf, clientFactory);
			}
		} else {
			if (bridgeClient != null) {
				logger.info("BRIDGE: empty / non-valid configuration submitted, disposing bridge client");
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
		updateBridge(conf);
	}

	private boolean isConfigurationValid(ServerConfiguration serverConfiguration) {
		boolean result = false;
		if (serverConfiguration.location != null && !serverConfiguration.location.isEmpty() &&
				serverConfiguration.sharedSpace != null && !serverConfiguration.sharedSpace.isEmpty()) {
			try {
				URL tmp = new URL(serverConfiguration.location);
				result = true;
			} catch (MalformedURLException mue) {
				logger.warning("BRIDGE: configuration with malformed URL supplied");
			}
		}
		return result;
	}
}
