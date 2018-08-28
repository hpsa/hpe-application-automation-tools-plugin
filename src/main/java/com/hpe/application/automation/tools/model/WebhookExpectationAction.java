package com.hpe.application.automation.tools.model;

import hudson.model.Action;

import javax.annotation.CheckForNull;

public class WebhookExpectationAction implements Action {
    public Boolean getExpectingToGetWebhookCall() {
        return isExpectingToGetWebhookCall;
    }

    public void setExpectingToGetWebhookCall(Boolean expectingToGetWebhookCall) {
        isExpectingToGetWebhookCall = expectingToGetWebhookCall;
    }

    private Boolean isExpectingToGetWebhookCall;

    public WebhookExpectationAction(Boolean isExpectingToGetWebhookCall) {
        this.isExpectingToGetWebhookCall = isExpectingToGetWebhookCall;
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