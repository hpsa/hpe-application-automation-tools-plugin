/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hp.application.automation.tools.octane.events;

import com.google.inject.Inject;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hp.application.automation.tools.octane.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.application.automation.tools.octane.configuration.ConfigurationListener;
import com.hp.application.automation.tools.octane.configuration.ServerConfiguration;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */

@Extension
public final class EventsService implements ConfigurationListener {
	private static final Logger logger = LogManager.getLogger(EventsService.class);

	private static EventsService extensionInstance;
	private JenkinsMqmRestClientFactory clientFactory;
	private EventsClient eventsClient;

	public static EventsService getExtensionInstance() {
		if (extensionInstance == null) {
			List<EventsService> extensions = Jenkins.getInstance().getExtensionList(EventsService.class);
			if (extensions.isEmpty()) {
				throw new RuntimeException("events service was not initialized properly");
			} else if (extensions.size() > 1) {
				throw new RuntimeException("events service expected to be singleton, found " + extensions.size() + " instances");
			} else {
				extensionInstance = extensions.get(0);
			}
		}
		return extensionInstance;
	}

	public void updateClient(ServerConfiguration conf) {
		if (conf.isValid()) {
			if (eventsClient != null) {
				eventsClient.update(conf);
			} else {
				eventsClient = new EventsClient(conf, clientFactory);
			}
		} else {
			if (eventsClient != null) {
				logger.info("empty / non-valid configuration submitted, disposing events client");
				eventsClient.dispose();
				eventsClient = null;
			}
		}
	}

	public void wakeUpClient() {
		if (eventsClient != null) {
			eventsClient.activate();
		}
	}

	public void dispatchEvent(CIEvent event) {
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
