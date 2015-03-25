package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.configuration.ConfigurationListener;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;

/**
 * Created by gullery on 22/03/2015.
 */

@Extension
public class MQMConfigurationListener implements ConfigurationListener {
	@Override
	public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
		EventDispatcher.updateClient(conf, oldConf);
	}
}
