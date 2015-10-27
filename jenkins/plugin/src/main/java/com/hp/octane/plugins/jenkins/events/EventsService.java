package com.hp.octane.plugins.jenkins.events;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationListener;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import hudson.Extension;
import jenkins.model.Jenkins;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */

@Extension
public final class EventsService implements ConfigurationListener {
	private static final Logger logger = Logger.getLogger(EventsService.class.getName());

	private static EventsService extensionInstance;
	private JenkinsMqmRestClientFactory clientFactory;
	private EventsClient eventsClient;

	public static EventsService getExtensionInstance() {
		if (extensionInstance == null) {
			List<EventsService> extensions = Jenkins.getInstance().getExtensionList(EventsService.class);
			if (extensions.isEmpty()) {
				throw new RuntimeException("EVENTS: events service was not initialized properly");
			} else if (extensions.size() > 1) {
				throw new RuntimeException("EVENTS: events service expected to be singleton, found " + extensions.size() + " instances");
			} else {
				extensionInstance = extensions.get(0);
			}
		}
		return extensionInstance;
	}

	public void updateClient(ServerConfiguration conf) {
		if (conf == null || conf.password == null ||
				conf.location == null || conf.location.equals("") ||
				conf.sharedSpace == null || conf.sharedSpace.equals("") ||
				conf.username == null || conf.username.equals("")) {
			logger.warning("EVENTS: bad configuration encountered, events client will not be " + (eventsClient == null ? "created" : "updated"));
		} else {
			if (eventsClient != null) {
				eventsClient.update(conf);
			} else {
				eventsClient = new EventsClient(conf, clientFactory);
			}
		}
	}

	public void wakeUpClient() {
		if (eventsClient != null) {
			eventsClient.activate();
		}
	}

	public void dispatchEvent(CIEventBase event) {
		if (eventsClient != null) {
			eventsClient.pushEvent(event);
		}
	}

	public List<EventsClient> getStatus() {
		if (eventsClient != null) {
			return Collections.singletonList(eventsClient);
		} else {
			return Collections.emptyList();
		}
	}

	public EventsClient getClient() {
		return eventsClient;
	}

	@Inject
	public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
		updateClient(conf);
	}
}
