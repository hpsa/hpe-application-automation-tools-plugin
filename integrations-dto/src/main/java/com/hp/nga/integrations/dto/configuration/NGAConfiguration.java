package com.hp.nga.integrations.dto.configuration;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 08/01/2016.
 * <p>
 * NGA Server configuration descriptor
 */

public interface NGAConfiguration extends DTOBase {

	String getUrl();

	NGAConfiguration setUrl(String url);

	Long getSharedSpace();

	NGAConfiguration setSharedSpace(Long sharedSpace);

	String getApiKey();

	NGAConfiguration setApiKey(String apiKey);

	String getSecret();

	NGAConfiguration setSecret(String secret);

	boolean isValid();
}
