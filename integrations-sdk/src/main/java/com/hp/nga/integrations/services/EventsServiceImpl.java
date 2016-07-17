package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.EventsService;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.connectivity.HttpMethod;
import com.hp.octane.integrations.dto.connectivity.OctaneRequest;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventsList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */

final class EventsServiceImpl implements EventsService {
	private static final Logger logger = LogManager.getLogger(EventsServiceImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private final SDKManager sdk;

	private static final class WaitMonitor {
		volatile boolean released;
	}

	private final List<CIEvent> events = Collections.synchronizedList(new ArrayList<CIEvent>());
	private final Object INIT_LOCKER = new Object();
	private final WaitMonitor WAIT_MONITOR = new WaitMonitor();
	private Thread worker;
	volatile boolean paused;

	private int MAX_SEND_RETRIES = 7;
	private int INITIAL_RETRY_PAUSE = 1739;
	private int DATA_SEND_INTERVAL = 1373;
	private int DATA_SEND_INTERVAL_IN_SUSPEND = 10 * 60 * 2;
	private int failedRetries;
	private int pauseInterval;
	volatile private boolean shuttingDown;

	EventsServiceImpl(SDKManager sdk) {
		this.sdk = sdk;
		activate();
	}

	public void publishEvent(CIEvent event) {
		events.add(event);
	}

	void activate() {
		resetCounters();
		if (worker == null || !worker.isAlive()) {
			synchronized (INIT_LOCKER) {
				if (worker == null || !worker.isAlive()) {
					worker = new Thread(new Runnable() {
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
				.setServer(sdk.getCIPluginServices().getServerInfo())
				.setEvents(new ArrayList<CIEvent>(events));
		String requestBody;
		boolean result = true;

		try {
			logger.info("sending " + eventsSnapshot.getEvents().size() + " event/s to '" + eventsSnapshot.getServer().getUrl() + "'...");
			OctaneRequest request = createEventsRequest(eventsSnapshot);
			OctaneResponse response;
			while (failedRetries < MAX_SEND_RETRIES) {
				response = sdk.getInternalService(OctaneRestService.class).obtainClient().execute(request);
				if (response.getStatus() == 200) {
					events.removeAll(eventsSnapshot.getEvents());
					logger.info("... done, left to send " + events.size() + " events");
					resetCounters();
					break;
				} else {
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

	private OctaneRequest createEventsRequest(CIEventsList events) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/json");
		OctaneRequest request = dtoFactory.newDTO(OctaneRequest.class)
				.setMethod(HttpMethod.PUT)
				.setUrl(sdk.getCIPluginServices().getOctaneConfiguration().getUrl() + "/internal-api/shared_spaces/" +
						sdk.getCIPluginServices().getOctaneConfiguration().getSharedSpace() + "/analytics/ci/events")
				.setHeaders(headers)
				.setBody(dtoFactory.dtoToJson(events));
		return request;
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
}
