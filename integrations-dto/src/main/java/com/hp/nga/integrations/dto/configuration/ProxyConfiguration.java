package com.hp.nga.integrations.dto.configuration;

/**
 * Created by gullery on 11/02/2016.
 */

public interface ProxyConfiguration {

	String getUrl();

	ProxyConfiguration setUrl(String url);

	String getUsername();

	ProxyConfiguration setUsername(String username);

	String getPassword();

	ProxyConfiguration setPassword(String password);
}
