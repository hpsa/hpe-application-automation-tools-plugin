package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.configuration.RestUtils;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.ModelBuilder;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */

public final class EventDispatcher {
	private static final int MAX_SEND_RETRIES = 7;
	private static final int INITIAL_RETRY_PAUSE = 2739;
	private static final int BREATH_PAUSE = 137;

	private static final Logger logger = Logger.getLogger(EventDispatcher.class.getName());

	private static final List<Client> clients = new ArrayList<Client>();

	@ExportedBean
	public static class Client {
		private final EventsList eventsList = new EventsList();
		private final Object locker = new Object();
		private Thread executor;

		private int CUSTOM_MAX_SEND_RETRIES;
		private int CUSTOM_INITIAL_RETRY_PAUSE;
		private int CUSTOM_BREATH_PAUSE;
		private boolean shuttingDown;
		private int failedRetries;
		private int pauseInterval;

		@Exported(inline = true)
		public String url;
		@Exported(inline = true)
		public String domain;
		@Exported(inline = true)
		public String project;
		@Exported(inline = true)
		public String lastErrorNote;
		@Exported(inline = true)
		public Date lastErrorTime;
		@Exported(inline = true)
		public String username;
		private String password;

		@Exported(inline = true)
		public boolean isActive() {
			return executor != null && executor.isAlive();
		}

		public Client(String url, String domain, String project, String username, String password) {
			this.CUSTOM_MAX_SEND_RETRIES = MAX_SEND_RETRIES;
			this.CUSTOM_INITIAL_RETRY_PAUSE = INITIAL_RETRY_PAUSE;
			this.CUSTOM_BREATH_PAUSE = BREATH_PAUSE;
			this.url = url;
			this.domain = domain;
			this.project = project;
			this.username = username;
			this.password = password;
		}

		public int pushEvent(CIEventBase event) {
			return eventsList.add(event);
		}

		//  TODO: there can be a race condition between suspension and activation; this can be fair case, yet further revision is needed
		private void activate() {
			resetCounters();
			if (executor == null || !executor.isAlive()) {
				synchronized (locker) {
					if (executor == null || !executor.isAlive()) {
						executor = new Thread(new Runnable() {
							@Override
							public void run() {
								int status;
								List<CIEventBase> localList;
								while (!shuttingDown) {
									try {
										if (eventsList.size() > 0) {
											logger.info("pushing " + eventsList.size() + " event/s to '" + url + "'...");
											localList = new ArrayList<CIEventBase>(eventsList.getEvents());
											Writer w = new StringWriter();
											new ModelBuilder().get(EventsList.class).writeTo(eventsList, Flavor.JSON.createDataWriter(localList, w));
											status = RestUtils.put(url, buildUrl(), username, password, w.toString());
											if (status == 200) {
												eventsList.clear(localList);
												resetCounters();
											} else {
												lastErrorNote = "push to MQM server failed; status: " + status;
												lastErrorTime = new Date();
												failedRetries++;
												logger.severe("push to '" + url + "' failed; status: '" + status + "'; total fails: " + failedRetries);
												if (failedRetries >= CUSTOM_MAX_SEND_RETRIES) {
													suspend();
												} else {
													Thread.sleep(pauseInterval);
													pauseInterval *= 2;
												}
											}
											logger.info("done; " + eventsList.size() + " more event/s is/are in queue for '" + url + "'");
										} else {
											Thread.sleep(CUSTOM_BREATH_PAUSE);
										}
									} catch (Exception e) {
										lastErrorNote = "push to MQM server failed; exception: '" + e.getMessage() + "'";
										lastErrorTime = new Date();
										failedRetries++;
										logger.severe("push to '" + url + "' failed; exception: '" + e.getMessage() + "'; total fails: " + failedRetries);
										if (failedRetries >= CUSTOM_MAX_SEND_RETRIES) {
											suspend();
										}
									}
								}
								logger.info("events client for '" + url + "' shuts down");
							}
						});
						executor.setDaemon(true);
						executor.start();
						logger.info("new events client initialized for '" + this.url + "'");
					}
				}
			}
		}

		private void suspend() {
			eventsList.clear();
			shuttingDown = true;
		}

		private void resetCounters() {
			shuttingDown = false;
			failedRetries = 0;
			pauseInterval = CUSTOM_INITIAL_RETRY_PAUSE;
		}

		private String buildUrl() {
			return "/api/domains/" + domain + "/projects/" + project + "/cia/events";
		}
	}

	public static void updateClient(ServerConfiguration conf) {
		updateClient(conf, null);
	}

	public static void updateClient(ServerConfiguration conf, ServerConfiguration oldConf) {
		synchronized (clients) {
			if (oldConf != null && !conf.location.equals(oldConf.location)) {
				for (Client client : clients) {
					if (client.url.equals(oldConf.location)) {
						clients.remove(client);
						break;
					}
				}
			}
			if (conf != null &&
					conf.location != null && !conf.location.equals("") &&
					conf.domain != null && !conf.domain.equals("") &&
					conf.project != null && !conf.project.equals("") &&
					conf.username != null && !conf.username.equals("") && conf.password != null) {
				updateClient(conf.location, conf.domain, conf.project, conf.username, conf.password);
			}
		}
	}

	public static void updateClient(String url, String domain, String project, String username, String password) {
		Client client = null;
		synchronized (clients) {
			for (Client c : clients) {
				if (c.url.equals(url)) {
					client = c;
					client.domain = domain;
					client.project = project;
					client.username = username;
					client.password = password;
				}
			}
			if (client == null) {
				client = new Client(url, domain, project, username, password);
				clients.add(client);
			}
		}
		client.activate();
	}

	public static void wakeUpClients() {
		synchronized (clients) {
			for (Client c : clients) {
				c.activate();
			}
		}
	}

	public static void dispatchEvent(CIEventBase event) {
		for (Client c : clients) {
			if (c.isActive()) c.pushEvent(event);
		}
	}

	public static List<Client> getStatus() {
		return clients;
	}
}
