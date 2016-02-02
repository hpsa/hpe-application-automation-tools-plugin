package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginService;
import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.api.EventsService;
import com.hp.nga.integrations.api.SDKServicesProvider;

class SDKServicesProviderImpl implements SDKServicesProvider {


	SDKServicesProviderImpl(CIPluginService ciPluginService) {
		if (ciPluginService == null) {
			throw new IllegalArgumentException("plugins services provider MUST NOT be null");
		}
	}

	public ConfigurationService getConfigurationService() {
		return null;
	}

	public EventsService getEventsService() {
		return null;
	}
}
