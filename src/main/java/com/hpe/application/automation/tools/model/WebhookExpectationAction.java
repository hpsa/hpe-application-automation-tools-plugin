package com.hpe.application.automation.tools.model;

import hudson.model.Action;
import hudson.model.TaskListener;

import javax.annotation.CheckForNull;

public class WebhookExpectationAction implements Action {


    private Boolean isExpectingToGetWebhookCall;
    private String serverUrl;


    public String getServerUrl() {
        return serverUrl;
    }




    public Boolean getExpectingToGetWebhookCall() {
        return isExpectingToGetWebhookCall;
    }

    public WebhookExpectationAction(Boolean isExpectingToGetWebhookCall, String serverUrl) {
        this.isExpectingToGetWebhookCall = isExpectingToGetWebhookCall;
        this.serverUrl = serverUrl;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }
}