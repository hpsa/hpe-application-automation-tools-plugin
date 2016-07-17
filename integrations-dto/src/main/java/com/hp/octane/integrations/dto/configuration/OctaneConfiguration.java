package com.hp.octane.integrations.dto.configuration;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 08/01/2016.
 * <p>
 * NGA Server configuration descriptor
 */

public interface OctaneConfiguration extends DTOBase {

	String getUrl();

	OctaneConfiguration setUrl(String url);

	Long getSharedSpace();

	OctaneConfiguration setSharedSpace(Long sharedSpace);

	String getApiKey();

	OctaneConfiguration setApiKey(String apiKey);

	String getSecret();

	OctaneConfiguration setSecret(String secret);

	boolean isValid();
}
