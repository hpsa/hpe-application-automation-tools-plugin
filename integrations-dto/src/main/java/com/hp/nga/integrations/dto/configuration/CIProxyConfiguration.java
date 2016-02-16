package com.hp.nga.integrations.dto.configuration;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 11/02/2016.
 *
 * Proxy configuration descriptor
 */

public interface CIProxyConfiguration extends DTOBase {

	String getUrl();

	CIProxyConfiguration setUrl(String url);

	String getUsername();

	CIProxyConfiguration setUsername(String username);

	String getPassword();

	CIProxyConfiguration setPassword(String password);
}
