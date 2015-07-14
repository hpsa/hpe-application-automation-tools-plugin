package com.hp.octane.plugins.jenkins.events;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import hudson.Extension;
import jenkins.model.Jenkins;

import java.util.ArrayList;
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
public final class EventsDispatcher {
	private static final Logger logger = Logger.getLogger(EventsClient.class.getName());

	private static EventsDispatcher extensionInstance;
	private JenkinsMqmRestClientFactory clientFactory;
	private final List<EventsClient> clients = new ArrayList<EventsClient>();

	public static EventsDispatcher getExtensionInstance() {
		List<EventsDispatcher> extensions;
		if (extensionInstance == null) {
			extensions = Jenkins.getInstance().getExtensionList(EventsDispatcher.class);
			if (extensions.isEmpty()) {
				throw new RuntimeException("Events Dispatcher was not initialized properly");
			} else {
				extensionInstance = extensions.get(0);
			}
		}
		return extensionInstance;
	}

	public void updateClient(ServerConfiguration conf) {
		updateClient(conf, null);
	}

	public void updateClient(ServerConfiguration conf, ServerConfiguration oldConf) {
		boolean updated = false;
		if (conf == null || conf.password == null ||
				conf.location == null || conf.location.equals("") ||
				conf.sharedSpace == null || conf.sharedSpace.equals("") ||
				conf.username == null || conf.username.equals("")) {
			logger.warning("bad configuration encountered, events client is not updated");
		} else {
			synchronized (clients) {
				for (EventsClient client : clients) {
					if (client.getLocation().equals(conf.location) &&
							client.getSharedSpace().equals(conf.sharedSpace)) {
						client.update(conf);
						client.activate();
						updated = true;
						break;
					}
				}
				if (!updated) {
					clients.add(new EventsClient(conf, clientFactory));
				}
			}
		}
	}

	public void wakeUpClients() {
		synchronized (clients) {
			for (EventsClient c : clients) {
				c.activate();
			}
		}
	}

	public void dispatchEvent(CIEventBase event) {
		synchronized (clients) {
			for (EventsClient c : clients) {
				if (c.isActive()) c.pushEvent(event);
			}
		}
	}

	public List<EventsClient> getStatus() {
		return new ArrayList<EventsClient>(clients);
	}

	public EventsClient getClient(String location, String sharedSpace) {
		EventsClient result = null;
		synchronized (clients) {
			for (EventsClient c : clients) {
				if (c.getLocation() != null && c.getLocation().equals(location) &&
						c.getSharedSpace() != null && c.getSharedSpace().equals(sharedSpace)) {
					result = c;
					break;
				}
			}
		}
		return result;
	}

	@Inject
	public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
		this.clientFactory = clientFactory;
	}
}
