package com.hp.octane.plugins.jenkins.notifications;

import com.hp.mqm.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
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

	private final List<CIEventBase> events = Collections.synchronizedList(new ArrayList<CIEventBase>());
	private final Object locker = new Object();
	private Thread worker;
	private MqmRestClient restClient;

	private int MAX_SEND_RETRIES = 7;
	private int INITIAL_RETRY_PAUSE = 1739;
	private int DATA_SEND_INTERVAL = 1373;
	private boolean shuttingDown;
	private int failedRetries;
	private int pauseInterval;

	private String url;
	private String domain;
	private String project;
	private String lastErrorNote;
	private Date lastErrorTime;
	private String username;
	private String password;

	public EventsClient(String url, String domain, String project, String username, String password, JenkinsMqmRestClientFactory clientFactory) {
		update(url, domain, project, username, password, clientFactory);
	}

	public void update(String url, String domain, String project, String username, String password, JenkinsMqmRestClientFactory clientFactory) {
		this.url = url;
		this.domain = domain;
		this.project = project;
		this.username = username;
		this.password = password;
		restClient = clientFactory.create(
				this.url,
				this.domain,
				this.project,
				this.username,
				this.password
		);
	}

	public void pushEvent(CIEventBase event) {
		events.add(event);
	}

	void activate() {
		resetCounters();
		if (worker == null || !worker.isAlive()) {
			synchronized (locker) {
				if (worker == null || !worker.isAlive()) {
					worker = new Thread(new Runnable() {
						@Override
						public void run() {
							while (!shuttingDown) {
								if (events.size() > 0) {
									if (!sendData()) suspend();
								} else {
									try {
										Thread.sleep(DATA_SEND_INTERVAL);
									} catch (InterruptedException ie) {
										logger.severe("EVENTS: worker thread of events client was interrupted; the client shuts down");
										suspend();
									}
								}
							}
							logger.severe("EVENTS: worker thread of events client shuts down");
						}
					});
					worker.setDaemon(true);
					worker.start();
					logger.info("EVENTS: new events client initialized for '" + this.url + "'");
				}
			}
		}
	}

	void suspend() {
		events.clear();
		shuttingDown = true;
	}

	private void resetCounters() {
		shuttingDown = false;
		failedRetries = 0;
		pauseInterval = INITIAL_RETRY_PAUSE;
	}

	private boolean sendData() {
		Writer w = new StringWriter();
		EventsList snapshot = new EventsList(events);
		String requestBody;
		boolean result = true;

		try {
			new ModelBuilder().get(EventsList.class).writeTo(snapshot, Flavor.JSON.createDataWriter(snapshot, w));
			requestBody = w.toString();
			logger.info("EVENTS: sending " + snapshot.getEvents().size() + " event/s to '" + url + "'...");
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
					logger.severe("EVENTS: send to '" + url + "' failed; total fails: " + failedRetries);
					if (failedRetries < MAX_SEND_RETRIES) Thread.sleep(pauseInterval *= 2);
				}
			}
			if (failedRetries == MAX_SEND_RETRIES) {
				logger.severe("EVENTS: max number of retries reached");
				result = false;
			}
		} catch (IOException ioe) {
			logger.severe("EVENTS: failed to send snapshot of " + snapshot.getEvents().size() + " events: " + ioe.getMessage() + "; dropping them all");
			events.removeAll(snapshot.getEvents());
		} catch (InterruptedException ie) {
			logger.severe("EVENTS: failed to send snapshot of " + snapshot.getEvents().size() + " events: " + ie.getMessage() + "; dropping them all");
			result = false;
		}
		return result;
	}

	@Exported(inline = true)
	public String getUrl() {
		return url;
	}

	@Exported(inline = true)
	public String getDomain() {
		return domain;
	}

	@Exported(inline = true)
	public String getProject() {
		return project;
	}

	@Exported(inline = true)
	public String getUsername() {
		return username;
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
}