package com.hp.nga.integrations.api;

import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.api.EventsService;

public interface SDKServicesProvider {

	ConfigurationService getConfigurationService();

	EventsService getEventsService();
}
