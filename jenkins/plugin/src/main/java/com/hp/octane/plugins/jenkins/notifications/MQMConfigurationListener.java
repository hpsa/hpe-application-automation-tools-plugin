package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.configuration.ConfigurationListener;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gullery on 22/03/2015.
 */

@Extension
public class MQMConfigurationListener implements ConfigurationListener {
	private static Logger logger = Logger.getLogger(MQMConfigurationListener.class.getName());

	@Override
	public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
		EventsDispatcher.getExtensionInstance().updateClient(conf, oldConf);
	}
}
