package com.hp.nga.integrations.api;

public interface SDKServicesProvider {

	CIPluginServices getCiPluginServices();

	ConfigurationService getConfigurationService();

	EventsService getEventsService();
}
