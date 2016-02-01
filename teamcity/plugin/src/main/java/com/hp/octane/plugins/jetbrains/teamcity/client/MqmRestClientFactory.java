package com.hp.octane.plugins.jetbrains.teamcity.client;

import com.hp.mqm.client.MqmConnectionConfig;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.MqmRestClientImpl;
import com.hp.mqm.client.UsernamePasswordProxyCredentials;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by linsha on 10/01/2016.
 */
public class MqmRestClientFactory {

    public static MqmRestClient create(String clientType, String location, String sharedSpace, String username, String password) {
        MqmConnectionConfig clientConfig = new MqmConnectionConfig(location, sharedSpace, username, password, clientType);
        URL locationUrl = null;
        try {
            locationUrl = new URL(clientConfig.getLocation());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        configureProxy(clientType, locationUrl, clientConfig, username);

        return new MqmRestClientImpl(clientConfig);
    }

    private static void configureProxy(String clientType, URL locationUrl, MqmConnectionConfig clientConfig, String username) {
        if (clientType.equals("HPE_TEAMCITY_PLUGIN")){
            if (isProxyNeeded(locationUrl.getHost())) {
                clientConfig.setProxyHost(getProxyHost());
                clientConfig.setProxyPort(getProxyPort());
                final String proxyUsername = getUsername();
                if (!StringUtils.isEmpty(proxyUsername)) {
                    clientConfig.setProxyCredentials(new UsernamePasswordProxyCredentials(username, getPassword()));
                }
            }

        }
    }

    private static String getProxyHost() {
        return null;
    }

    private static Integer getProxyPort() {
        return null;
    }

    private static String getUsername() {
        return null;
    }

    private static String getPassword() {
        return null;
    }

    private static boolean isProxyNeeded(final String host) {
        return false;
    }
}
