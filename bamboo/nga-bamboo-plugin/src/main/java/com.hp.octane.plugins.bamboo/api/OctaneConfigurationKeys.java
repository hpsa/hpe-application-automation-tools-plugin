package com.hp.octane.plugins.bamboo.api;

public interface OctaneConfigurationKeys {
	String BAMBOO_INSTANCE_PREFIX = "bamboo-instance-";
	String PLUGIN_PREFIX = "com.hp.octane.plugins.bamboo.";
	String OCTANE_URL = PLUGIN_PREFIX + "octaneUrl";
	String ACCESS_KEY = PLUGIN_PREFIX + "accessKey";
	String API_SECRET = PLUGIN_PREFIX + "apiSecret";
	String IMPERSONATION_USER = PLUGIN_PREFIX + "userName";
	String SHARED_SPACE_ID = PLUGIN_PREFIX + "sharedSpaceId";
	String UUID = "uuid";
}
