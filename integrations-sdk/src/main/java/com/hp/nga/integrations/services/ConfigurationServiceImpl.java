package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.services.configuration.NGAConfiguration;

class ConfigurationServiceImpl implements ConfigurationService {

	public void configurationChanged() {
		//  example code
		NGAConfiguration newConfig = SDKFactory.getCIPluginServices().getNGAConfiguration();
		//  do anything with this new config
	}
}
