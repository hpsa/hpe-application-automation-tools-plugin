package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginService;
import com.hp.nga.integrations.api.ConfigurationService;

public class ConfigurationServiceImpl implements ConfigurationService {
	private final CIPluginService ciPluginService;

	ConfigurationServiceImpl(CIPluginService ciPluginService) {
		this.ciPluginService = ciPluginService;
	}

	public void configurationChanged() {
	}
}
