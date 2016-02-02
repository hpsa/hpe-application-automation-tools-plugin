package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.api.SDKServicesProvider;
import com.hp.nga.integrations.services.configuration.NGAConfiguration;

public class ConfigurationServiceImpl implements ConfigurationService {
	private final SDKServicesProvider sdkServicesProvider;

	ConfigurationServiceImpl(SDKServicesProvider servicesProvider) {
		if (servicesProvider == null) {
			throw new IllegalArgumentException("services provider MUST NOT be null");
		}
		this.sdkServicesProvider = servicesProvider;
	}

	public void configurationChanged() {
		//  example code
		NGAConfiguration newConfig = sdkServicesProvider.getCiPluginServices().getNGAConfiguration();
		//  do anything with this new config
	}
}
