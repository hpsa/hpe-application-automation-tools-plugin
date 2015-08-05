package com.hp.octane.plugins.jenkins.bridge;

import com.hp.octane.plugins.jenkins.configuration.ConfigurationListener;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.Extension;

/**
 * Created by gullery on 04/08/2015.
 *
 * MQM Configuration listener reacts upon configuration changes, specifically turns on/off the abridged connectivity according the the 'abridged' flag
 */

@Extension
public class MQMConfigurationListener  implements ConfigurationListener {
	@Override
	public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {

	}
}
