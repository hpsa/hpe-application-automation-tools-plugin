package com.hp.nga.integrations.dto.configuration;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 11/02/2016.
 */

public interface ProxyConfiguration extends DTOBase {

	String getUrl();

	ProxyConfiguration setUrl(String url);

	String getUsername();

	ProxyConfiguration setUsername(String username);

	String getPassword();

	ProxyConfiguration setPassword(String password);
}
