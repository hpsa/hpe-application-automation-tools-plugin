// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.client;

import com.hp.octane.plugins.jenkins.notifications.EventsClient;
import com.hp.octane.plugins.jenkins.notifications.EventsDispatcher;

import java.util.List;

class JenkinsInsightEventPublisher implements RetryModel.EventPublisher {

    @Override
    public boolean isSuspended() {
        List<EventsClient> status = EventsDispatcher.getExtensionInstance().getStatus();
        synchronized (status) { // accessing internal structure, synchronization is mandatory
            if (status.isEmpty()) {
                return true;
            }
            EventsClient eventsClient = status.get(0);
            return !eventsClient.isActive() || eventsClient.isPaused();
        }
    }

    @Override
    public void resume() {
        EventsDispatcher.getExtensionInstance().wakeUpClients();
    }
}