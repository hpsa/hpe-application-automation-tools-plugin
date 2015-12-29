package com.hp.octane.plugins.jenkins.events;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gullery on 21/04/2015.
 * <p/>
 * Event Client is a service of dispatching events to a single MQM Server Context (server : sharedspace)
 */

@ExportedBean
public class EventsClient {
	private static final Logger logger = Logger.getLogger(EventsClient.class.getName());

	private static final class WaitMonitor {
		volatile boolean released;
	}

	private final List<CIEventBase> events = Collections.synchronizedList(new ArrayList<CIEventBase>());
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
		logger.info("EVENTS: client initialized for '" + this.mqmConfig.location + "' (SP: " + this.mqmConfig.sharedSpace + ")");
	}

	public void update(ServerConfiguration newConfig) {
		mqmConfig = new ServerConfiguration(newConfig.location, newConfig.sharedSpace, newConfig.username, newConfig.password, newConfig.impersonatedUser);
		activate();
		logger.info("EVENTS: updated for '" + mqmConfig.location + "' (SP: " + mqmConfig.sharedSpace + ")");
	}

	public void pushEvent(CIEventBase event) {
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
									logger.severe("EVENTS: Exception while events sending: " + e.getMessage());
								}
							}
							logger.info("EVENTS: worker thread of events client stopped");
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
				logger.warning("EVENTS: interruption happened while shutting down worker thread");
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
		Writer w = new StringWriter();
		EventsList snapshot = new EventsList(events);
		String requestBody;
		boolean result = true;
		MqmRestClient restClient = restClientFactory.create(mqmConfig.location, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);

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
		synchronized (WAIT_MONITOR) {
			WAIT_MONITOR.released = false;
			paused = true;
			while (!WAIT_MONITOR.released && new Date().getTime() - waitStart < timeout) {
				try {
					WAIT_MONITOR.wait(timeout);
				} catch (InterruptedException ie) {
					logger.warning("EVENTS: waiting period was interrupted: " + ie.getMessage());
				}
			}
			paused = false;
			if (WAIT_MONITOR.released) {
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
	public String getImpersonatedUser() {
		return mqmConfig.impersonatedUser;
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