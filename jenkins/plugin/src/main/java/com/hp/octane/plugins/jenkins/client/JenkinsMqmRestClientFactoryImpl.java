// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.client;

import com.hp.mqm.client.MqmConnectionConfig;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.MqmRestClientImpl;
import com.hp.mqm.client.UsernamePasswordProxyCredentials;
import hudson.Extension;
import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
public class JenkinsMqmRestClientFactoryImpl implements JenkinsMqmRestClientFactory {
    private static final Logger logger = Logger.getLogger(JenkinsMqmRestClientFactoryImpl.class.getName());
    private static final String CLIENT_TYPE = "HPE_JENKINS_PLUGIN";
    private static MqmRestClient mqmRestClient;

    @Override
    public synchronized MqmRestClient obtain(String location, String sharedSpace, String username, String password) {
        if(mqmRestClient == null) {

            mqmRestClient = create( location,  sharedSpace,  username,  password);
            logger.info("NGA REST Clint: initialized for " + location + " - " + sharedSpace + " - " + username);
        }
        return mqmRestClient;
    }

    @Override
    public MqmRestClient obtainTemp(String location, String sharedSpace, String username, String password) {

        MqmRestClient mqmRestClientTemp = create( location,  sharedSpace,  username,  password);
        logger.info("NGA REST Clint: initialized for " + location + " - " + sharedSpace + " - " + username);
        return mqmRestClientTemp;
    }

    @Override
    public synchronized void updateMqmRestClient(String location, String sharedSpace, String username, String password){

        mqmRestClient = create( location,  sharedSpace,  username,  password);
        logger.info("NGA REST Clint: updated to " + location + " - " + sharedSpace + " - " + username);
    }

    private MqmRestClient create(String location, String sharedSpace, String username, String password){
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
        MqmRestClient restClient = new MqmRestClientImpl(clientConfig);
        return restClient;
    }

    private String getProxyHost() {
        final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
        if (proxyConfiguration != null) {
            return proxyConfiguration.name;
        }
        return null;
    }

    private Integer getProxyPort() {
        final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
        if (proxyConfiguration != null) {
            return proxyConfiguration.port;
        }
        return null;
    }

    private String getUsername() {
        final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
        if (proxyConfiguration != null) {
            return proxyConfiguration.getUserName();
        }
        return null;
    }

    private String getPassword() {
        final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
        if (proxyConfiguration != null) {
            return proxyConfiguration.getPassword();
        }
        return null;
    }

    private boolean isProxyNeeded(final String host) {
        final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
        if (proxyConfiguration != null && !StringUtils.isEmpty(proxyConfiguration.name)) {
            // if any patterns match the host, we will not use proxy
            final List<Pattern> patterns = proxyConfiguration.getNoProxyHostPatterns();
            if (patterns != null) {
                for (final Pattern pattern : patterns) {
                    final Matcher matcher = pattern.matcher(host);
                    if (matcher.matches()) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
