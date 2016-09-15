package com.emyoli.nga.api;

public interface OctaneConfigurationKeys {

    String BAMBOO_INSTANCE_PREFIX = "bamboo-instance-";
    String PLUGIN_PREFIX = "com.emyoli.nga.bamboo-plugin.";
    String NGA_URL = PLUGIN_PREFIX + "ngaUrl";
    String API_KEY = PLUGIN_PREFIX + "apiKey";
    String API_SECRET = PLUGIN_PREFIX + "apiSecret";
    String USER_TO_USE = PLUGIN_PREFIX + "userName";
    String SHARED_SPACE_ID = PLUGIN_PREFIX + "sharedSpaceId";

}
