package com.hp.nga.integrations.dto.configuration;

/**
 * Created by gullery on 08/01/2016.
 * <p>
 * NGA Server configuration descriptor
 */

public interface NGAConfiguration {

	String getUrl();

	NGAConfiguration setUrl(String url);

	Long getSharedSpace();

	NGAConfiguration setSharedSpace(Long sharedSpace);

	String getClientId();

	NGAConfiguration setClientId(String clientId);

	String getApiKey();

	NGAConfiguration setApiKey(String apiKey);
}
