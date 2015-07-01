// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.client;

import com.hp.octane.plugins.jenkins.notifications.EventsClient;
import com.hp.octane.plugins.jenkins.notifications.EventsDispatcher;
import hudson.Extension;

@Extension
public class JenkinsInsightEventPublisher implements EventPublisher {

    @Override
    public boolean isSuspended(String location, String sharedSpace) {
        EventsClient client = EventsDispatcher.getExtensionInstance().getClient(location, sharedSpace);
        return client == null || client.isSuspended();
    }

    @Override
    public void resume() {
        EventsDispatcher.getExtensionInstance().wakeUpClients();
    }
}