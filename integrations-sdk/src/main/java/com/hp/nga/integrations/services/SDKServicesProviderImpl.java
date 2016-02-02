package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.api.EventsService;
import com.hp.nga.integrations.api.SDKServicesProvider;

class SDKServicesProviderImpl implements SDKServicesProvider {
	private final CIPluginServices ciPluginServices;

	SDKServicesProviderImpl(CIPluginServices ciPluginServices) {
		if (ciPluginServices == null) {
			throw new IllegalArgumentException("plugins services provider MUST NOT be null");
		}

		this.ciPluginServices = ciPluginServices;
	}

	public CIPluginServices getCiPluginServices() {
		return ciPluginServices;
	}

	public ConfigurationService getConfigurationService() {
		return new ConfigurationServiceImpl(this);
	}

	public EventsService getEventsService() {
		return new EventsServiceImpl();
	}
}
