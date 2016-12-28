package com.hp.octane.plugins.bamboo.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by lazara on 28/12/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OctaneConnectionDTO {
    private String octaneUrl;
    private String accessKey;
    private String apiSecret;
    private String userName;
    public String getUserName() { return userName;}

    public void setUserName(String userName) { this.userName = userName;}

    public String getOctaneUrl() {
        return octaneUrl;
    }

    public void setOctaneUrl(String octaneUrl) {
        this.octaneUrl = octaneUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }
}