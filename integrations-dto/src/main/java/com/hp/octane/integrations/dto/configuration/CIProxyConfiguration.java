package com.hp.octane.integrations.dto.configuration;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 11/02/2016.
 * <p>
 * Proxy configuration descriptor
 */

public interface CIProxyConfiguration extends DTOBase {

	String getHost();

	CIProxyConfiguration setHost(String host);

	Integer getPort();

	CIProxyConfiguration setPort(Integer port);

	String getUsername();

	CIProxyConfiguration setUsername(String username);

	String getPassword();

	CIProxyConfiguration setPassword(String password);
}
