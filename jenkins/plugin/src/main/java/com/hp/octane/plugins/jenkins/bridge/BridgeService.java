package com.hp.octane.plugins.jenkins.bridge;

import com.hp.octane.plugins.jenkins.configuration.ConfigurationListener;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gullery on 05/08/2015.
 * <p/>
 * Bridge Service meant to provide an abridged connectivity functionality
 * The only APIs to be exposed is the basic management of abridged clients
 */

@Extension
public class BridgeService implements ConfigurationListener {
	private static final Logger logger = Logger.getLogger(BridgeService.class.getName());

	private static BridgeService extensionInstance;

	public static BridgeService getExtensionInstance() {
		if (extensionInstance == null) {
			List<BridgeService> extensions = Jenkins.getInstance().getExtensionList(BridgeService.class);
			if (extensions.isEmpty()) {
				throw new RuntimeException("Events Dispatcher was not initialized properly");
			} else {
				extensionInstance = extensions.get(0);
			}
		}
		return extensionInstance;
	}

	@Override
	public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
		//  TODO: add configuration processing
		//  if abridged - add task poller
		//  if not - remove task poller if exists
	}
}
