package com.hp.octane.plugins.jetbrains.teamcity.client;

import com.hp.mqm.client.MqmConnectionConfig;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.UsernamePasswordProxyCredentials;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linsha on 03/01/2016.
 */
public class TeamCityMqmRestClientFactory {
    private static final String CLIENT_TYPE = "HPE_TEAMCITY_PLUGIN";


    public MqmRestClient create(String location, String sharedSpace, String username, String password) {
        MqmConnectionConfig clientConfig = new MqmConnectionConfig(location, sharedSpace, username, password, CLIENT_TYPE);
        URL locationUrl = null;
        try {
            locationUrl = new URL(clientConfig.getLocation());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        if (isProxyNeeded(locationUrl.getHost())) {
            clientConfig.setProxyHost(getProxyHost());
            clientConfig.setProxyPort(getProxyPort());
            final String proxyUsername = getUsername();
            if (!StringUtils.isEmpty(proxyUsername)) {
                clientConfig.setProxyCredentials(new UsernamePasswordProxyCredentials(username, getPassword()));
            }
        }
        return com.hp.mqm.client.MqmRestClientFactory.create(clientConfig);
    }

    private String getProxyHost() {
        return null;
    }

    private Integer getProxyPort() {
        return null;
    }

    private String getUsername() {
        return null;
    }

    private String getPassword() {
        return null;
    }

    private boolean isProxyNeeded(final String host) {
        return false;
    }
}



