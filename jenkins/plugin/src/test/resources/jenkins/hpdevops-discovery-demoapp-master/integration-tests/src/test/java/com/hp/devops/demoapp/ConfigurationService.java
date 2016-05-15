package com.hp.devops.demoapp;

import javax.annotation.PostConstruct;

/**
 * User: belozovs
 * Date: 11/25/14
 * Description
 */
public class ConfigurationService {

    private static ConfigurationService instance = new ConfigurationService();

    private String protocol = "http";
    private String hostName = "localhost";
    private int port = 9999;
    private String basePath = "/api";
    private String proxyHost = "";  //rhvwebcachevip.bastion.europe.hp.com
    private int proxyPort = 0;  //8080

    private ConfigurationService(){

    }
    public static ConfigurationService getInstance(){
        if(System.getProperty("hostname")!=null){
            instance.hostName = System.getProperty("hostname");
        }
        if(System.getProperty("port")!=null){
            instance.port = Integer.parseInt(System.getProperty("port"));
        }
        if(System.getProperty("protocol")!=null){
            instance.protocol = System.getProperty("protocol");
        }
        if(System.getProperty("basepath")!=null){
            instance.basePath = System.getProperty("basepath");
        }
        if(System.getProperty("proxyhost")!=null){
            instance.proxyHost =System.getProperty("proxyhost");
        }
        if(System.getProperty("proxyport")!=null){
            instance.proxyPort =Integer.parseInt(System.getProperty("proxyport"));
        }
        System.out.println("Starting the test for " + instance.protocol + "://" + instance.hostName + ":" + instance.port + instance.basePath);
        if(!instance.proxyHost.isEmpty()){
            System.out.println("The tests will run via proxy: " + instance.proxyHost + ":" + instance.proxyPort);
        }

        return instance;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getBaseUri(){
        return protocol + "://" + hostName;
    }
}
