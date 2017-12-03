/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.octane.events;

import com.google.inject.Inject;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactoryImpl;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationListener;
import com.hpe.application.automation.tools.octane.configuration.ServerConfiguration;
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

	private JenkinsMqmRestClientFactory clientFactory;
	private EventsClient eventsClient;

	public static EventsService getExtensionInstance() {
			return Jenkins.getInstance().getExtensionList(EventsService.class).get(0);
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
