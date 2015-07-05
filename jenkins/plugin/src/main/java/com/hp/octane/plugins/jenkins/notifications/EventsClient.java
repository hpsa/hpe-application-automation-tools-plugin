package com.hp.octane.plugins.jenkins.notifications;

import com.hp.mqm.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.ModelBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gullery on 21/04/2015.
 */

@ExportedBean
public class EventsClient {
	private static final Logger logger = Logger.getLogger(EventsClient.class.getName());

	private static final class WaitMonitor {
		volatile boolean released;
	}

	private final List<CIEventBase> events = Collections.synchronizedList(new ArrayList<CIEventBase>());
	private final Object initLocker = new Object();
	private final WaitMonitor waitMonitor = new WaitMonitor();
	private Thread worker;
	volatile boolean paused;

	//  TODO: needs redesign, or client should be reusable or no relogin etc logic is needed (each time new login is a performance killer though)
	private JenkinsMqmRestClientFactory restClientFactory;

	private int MAX_SEND_RETRIES = 7;
	private int INITIAL_RETRY_PAUSE = 1739;
	private int DATA_SEND_INTERVAL = 1373;
	private int DATA_SEND_INTERVAL_IN_SUSPEND = 10 * 60 * 2;
	private boolean shuttingDown;
	private int failedRetries;
	private int pauseInterval;

	private ServerConfiguration mqmConfig;
	private String lastErrorNote;
	private Date lastErrorTime;

	public EventsClient(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory) {
		this.mqmConfig = mqmConfig;
		this.restClientFactory = clientFactory;
		activate();
	}

	public void update(ServerConfiguration mqmConfig) {
		this.mqmConfig = mqmConfig;
	}

	public void pushEvent(CIEventBase event) {
		events.add(event);
	}

	void activate() {
		resetCounters();
		if (worker == null || !worker.isAlive()) {
			synchronized (initLocker) {
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
									logger.severe("EVENTS: Exception while events sending: " + e.getMessage());
								}
							}
							logger.info("EVENTS: worker thread of events client shuts down");
						}
					});
					worker.setDaemon(true);
					worker.setName("EventsClientWorker");
					worker.start();
					logger.info("EVENTS: new events client initialized for '" + this.mqmConfig.location + "'");
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

	private void resetCounters() {
		shuttingDown = false;
		failedRetries = 0;
		pauseInterval = INITIAL_RETRY_PAUSE;
		synchronized (waitMonitor) {
			if (worker != null && worker.getState() == Thread.State.TIMED_WAITING) {
				waitMonitor.released = true;
				waitMonitor.notify();
			}
		}
	}

	private boolean sendData() {
		Writer w = new StringWriter();
		EventsList snapshot = new EventsList(events);
		String requestBody;
		boolean result = true;
		MqmRestClient restClient = restClientFactory.create(
				mqmConfig.location,
				mqmConfig.sharedSpace,
				mqmConfig.username,
				mqmConfig.password
		);

		try {
			new ModelBuilder().get(EventsList.class).writeTo(snapshot, Flavor.JSON.createDataWriter(snapshot, w));
			requestBody = w.toString();
			logger.info("EVENTS: sending " + snapshot.getEvents().size() + " event/s to '" + mqmConfig.location + "'...");
			while (failedRetries < MAX_SEND_RETRIES) {
				if (restClient.putEvents(requestBody)) {
					events.removeAll(snapshot.getEvents());
					logger.info("EVENTS: ... done, left to send " + events.size() + " events");
					resetCounters();
					break;
				} else {
					lastErrorNote = "EVENTS: send to MQM server failed";
					lastErrorTime = new Date();
					failedRetries++;
					logger.severe("EVENTS: send to '" + mqmConfig.location + "' failed; total fails: " + failedRetries);
					if (failedRetries < MAX_SEND_RETRIES) {
						doBreakableWait(pauseInterval *= 2);
					}
				}
			}
			if (failedRetries == MAX_SEND_RETRIES) {
				logger.severe("EVENTS: max number of retries reached");
				result = false;
			}
		} catch (IOException ioe) {
			logger.severe("EVENTS: failed to send snapshot of " + snapshot.getEvents().size() + " events: " + ioe.getMessage() + "; dropping them all");
			events.removeAll(snapshot.getEvents());
		}
		return result;
	}

	private void doBreakableWait(long timeout) {
		logger.info("EVENTS: entering waiting period of " + timeout + "ms");
		long waitStart = new Date().getTime();
		synchronized (waitMonitor) {
			waitMonitor.released = false;
			paused = true;
			while (!waitMonitor.released && new Date().getTime() - waitStart < timeout) {
				try {
					waitMonitor.wait(timeout);
				} catch (InterruptedException ie) {
					logger.warning("EVENTS: waiting period was interrupted: " + ie.getMessage());
				}
			}
			paused = false;
			if (waitMonitor.released) {
				logger.info("EVENTS: pause finished on demand");
			} else {
				logger.info("EVENTS: pause finished timely");
			}
		}
	}

	@Exported(inline = true)
	public String getLocation() {
		return mqmConfig.location;
	}

	@Exported(inline = true)
	public String getSharedSpace() {
		return mqmConfig.sharedSpace;
	}

	@Exported(inline = true)
	public String getUsername() {
		return mqmConfig.username;
	}

	@Exported(inline = true)
	public String getLastErrorNote() {
		return lastErrorNote;
	}

	@Exported(inline = true)
	public Date getLastErrorTime() {
		return lastErrorTime;
	}

	@Exported(inline = true)
	public boolean isActive() {
		return worker != null && worker.isAlive();
	}

	@Exported(inline = true)
	public boolean isPaused() {
		return paused;
	}

	public boolean isSuspended() {
		return !isActive() || isPaused();
	}
}