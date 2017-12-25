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

import com.hp.mqm.client.MqmRestClient;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventsList;
import com.hpe.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hpe.application.automation.tools.octane.configuration.ServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EventsClient {
	private static final Logger logger = LogManager.getLogger(EventsClient.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	private static final class WaitMonitor {
		volatile boolean released;
	}

	private final List<CIEvent> events = Collections.synchronizedList(new ArrayList<CIEvent>());
	private final Object INIT_LOCKER = new Object();
	private final WaitMonitor WAIT_MONITOR = new WaitMonitor();
	private Thread worker;
	volatile boolean paused;

	private JenkinsMqmRestClientFactory restClientFactory;

	private int MAX_SEND_RETRIES = 7;
	private int INITIAL_RETRY_PAUSE = 1739;
	private int DATA_SEND_INTERVAL = 1373;
	private int DATA_SEND_INTERVAL_IN_SUSPEND = 10 * 60 * 2;
	private int failedRetries;
	private int pauseInterval;
	volatile private boolean shuttingDown;

	private ServerConfiguration mqmConfig;
	private String lastErrorNote;
	private Date lastErrorTime;

	public EventsClient(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password, mqmConfig.impersonatedUser);
		this.restClientFactory = clientFactory;
		activate();
		logger.info("client initialized for '" + this.mqmConfig.location + "'; SP: " + this.mqmConfig.sharedSpace + "; access key: " + mqmConfig.username);
	}

	public void update(ServerConfiguration newConfig) {
		mqmConfig = new ServerConfiguration(newConfig.location, newConfig.sharedSpace, newConfig.username, newConfig.password, newConfig.impersonatedUser);
		activate();
		logger.info("client updated to '" + mqmConfig.location + "'; SP: " + mqmConfig.sharedSpace + "; access key: " + newConfig.username);
	}

	public void pushEvent(CIEvent event) {
		events.add(event);
	}

	void activate() {
		resetCounters();
		if (worker == null || !worker.isAlive()) {
			synchronized (INIT_LOCKER) {
				if (worker == null || !worker.isAlive()) {
					worker = new Thread(new Runnable() {
						@Override
						public void run() {
							while (!shuttingDown) {
								try {
									if (events.size() > 0) {
										if (!sendData()) suspend();
									}
									Thread.sleep(DATA_SEND_INTERVAL);
								} catch (Exception e) {
									logger.error("failed to send events", e);
								}
							}
							logger.info("worker thread of events client stopped");
						}
					});
					worker.setDaemon(true);
					worker.setName("EventsClientWorker");
					worker.start();
				}
			}
		}
	}

	void suspend() {
		events.clear();
		failedRetries = MAX_SEND_RETRIES - 1;
		doBreakableWait(DATA_SEND_INTERVAL_IN_SUSPEND);
		//shuttingDown = true;
	}

	void dispose() {
		events.clear();
		if (worker != null) {
			shuttingDown = true;
			try {
				worker.join();
			} catch (InterruptedException ie) {
				logger.info("interruption happened while shutting down worker thread", ie);
			} finally {
				if (worker.isAlive()) {
					worker.interrupt();
				}
			}
		}
	}

	private void resetCounters() {
		shuttingDown = false;
		failedRetries = 0;
		pauseInterval = INITIAL_RETRY_PAUSE;
		synchronized (WAIT_MONITOR) {
			if (worker != null && worker.getState() == Thread.State.TIMED_WAITING) {
				WAIT_MONITOR.released = true;
				WAIT_MONITOR.notify();
			}
		}
	}

	private boolean sendData() {
		CIEventsList eventsSnapshot = dtoFactory.newDTO(CIEventsList.class)
				.setServer(new CIJenkinsServicesImpl().getServerInfo())
				.setEvents(new ArrayList<CIEvent>(events));
		String requestBody;
		boolean result = true;
		MqmRestClient restClient = restClientFactory.obtain(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		String eventsSummary = "";
		for (CIEvent event : eventsSnapshot.getEvents()) {
			eventsSummary += event.getProject() + ":" + event.getBuildCiId() + ":" + event.getEventType() + ", ";
		}
		eventsSummary = eventsSummary.substring(0, eventsSummary.length() - 2);

		try {
			requestBody = dtoFactory.dtoToJson(eventsSnapshot);
			logger.info("sending events [" + eventsSummary + "] to '" + mqmConfig.location + "'...");
			while (failedRetries < MAX_SEND_RETRIES) {
				if (restClient.putEvents(requestBody)) {
					events.removeAll(eventsSnapshot.getEvents());
					logger.info("... done, left to send " + events.size() + " events");
					resetCounters();
					break;
				} else {
					lastErrorNote = "send to MQM server failed";
					lastErrorTime = new Date();
					failedRetries++;

					if (failedRetries < MAX_SEND_RETRIES) {
						doBreakableWait(pauseInterval *= 2);
					}
				}
			}
			if (failedRetries == MAX_SEND_RETRIES) {
				logger.error("max number of retries reached");
				result = false;
			}
		} catch (Exception e) {
			logger.error("failed to send snapshot of " + eventsSnapshot.getEvents().size() + " events: " + e.getMessage() + "; dropping them all", e);
			events.removeAll(eventsSnapshot.getEvents());
		}
		return result;
	}

	private void doBreakableWait(long timeout) {
		logger.info("entering waiting period of " + timeout + "ms");
		long waitStart = new Date().getTime();
		synchronized (WAIT_MONITOR) {
			WAIT_MONITOR.released = false;
			paused = true;
			while (!WAIT_MONITOR.released && new Date().getTime() - waitStart < timeout) {
				try {
					WAIT_MONITOR.wait(timeout);
				} catch (InterruptedException ie) {
					logger.info("waiting period was interrupted", ie);
				}
			}
			paused = false;
			if (WAIT_MONITOR.released) {
				logger.info("pause finished on demand");
			} else {
				logger.info("pause finished timely");
			}
		}
	}

	public String getLocation() {
		return mqmConfig.location;
	}

	public String getSharedSpace() {
		return mqmConfig.sharedSpace;
	}

	public String getUsername() {
		return mqmConfig.username;
	}

	public boolean isActive() {
		return worker != null && worker.isAlive();
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isSuspended() {
		return !isActive() || isPaused();
	}
}