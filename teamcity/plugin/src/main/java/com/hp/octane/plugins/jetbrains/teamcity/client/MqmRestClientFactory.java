package com.hp.octane.plugins.jetbrains.teamcity.client;

import com.hp.mqm.client.MqmConnectionConfig;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.MqmRestClientImpl;
import com.hp.mqm.client.UsernamePasswordProxyCredentials;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.ConfigurationService;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by linsha on 10/01/2016.
 */
public class MqmRestClientFactory {

    private static String host;
    private static Integer port;
    private static String userName;
    private static  String password;

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
        if (clientType.equals(ConfigurationService.CLIENT_TYPE)){
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
        return host;
    }

    private static Integer getProxyPort() {
        return port;
    }

    private static String getUsername() {
        return userName;
    }

    private static String getPassword() {
        return password;
    }

    private static boolean isProxyNeeded(final String str) {
        Map<String,String> propertiesMap = parseProperties(System.getenv("TEAMCITY_SERVER_OPTS"));

        if(propertiesMap.get("Dhttps.proxyHost")==null){
            return false;
        }
        host = propertiesMap.get("Dhttps.proxyHost");
        if(propertiesMap.get("Dhttps.proxyPort")!=null){
            port = Integer.parseInt(propertiesMap.get("Dhttps.proxyPort"));
        }

        return true;
/*
                -Dproxyset=true
                -Dhttp.proxyHost=proxy.domain.com
                -Dhttp.proxyPort=8080
                -Dhttp.nonProxyHosts=domain.com
                -Dhttps.proxyHost=web-proxy.il.hpecorp.net
                -Dhttps.proxyPort=8080
                -Dhttps.nonProxyHosts=domain.com
                */

    }

    private static Map<String,String> parseProperties(String internalProperties){
        Map<String,String> propertiesMap = new HashMap();
        if (internalProperties != null) {
            String[] properties  = internalProperties.split(" -");
            for(String str : Arrays.asList(properties)){
                String[] split = str.split("=");
                if(split.length ==2) {
                    propertiesMap.put(split[0], split[1]);
                }
            }

        }
        return propertiesMap;
    }
}
