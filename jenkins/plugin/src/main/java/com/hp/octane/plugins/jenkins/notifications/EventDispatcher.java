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
	private static final int INITIAL_RETRY_PAUSE = 273;
	private static final int BREATH_PAUSE = 137;

	private static final Logger logger = Logger.getLogger(EventDispatcher.class.getName());

	private static final List<Client> clients = new ArrayList<Client>();

	@ExportedBean
	public static class Client {
		private final EventsList eventsList = new EventsList();
		private Thread executor;
		private int CUSTOM_MAX_SEND_RETRIES;
		private int CUSTOM_INITIAL_RETRY_PAUSE;
		private int CUSTOM_BREATH_PAUSE;
		private boolean shuttingDown;
		private int failedRetries;
		private String password;

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

		public void activate() {
			eventsList.clear();
			shuttingDown = false;
			failedRetries = 0;
			executor = new Thread(new Runnable() {
				@Override
				public void run() {
					int status;
					int suspendTime = CUSTOM_INITIAL_RETRY_PAUSE;
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
									failedRetries = 0;
									suspendTime = CUSTOM_INITIAL_RETRY_PAUSE;
								} else {
									lastErrorNote = "push to MQM server failed; status: " + status;
									lastErrorTime = new Date();
									failedRetries++;
									logger.severe("push to '" + url + "' failed; status: '" + status + "'; total fails: " + failedRetries);
									if (failedRetries >= CUSTOM_MAX_SEND_RETRIES) {
										eventsList.clear();
									//	shuttingDown = true;
									} else {
										Thread.sleep(suspendTime * 1000);
										suspendTime *= 2;
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
								eventsList.clear();
							//	shuttingDown = true;
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

		public void dispose() {
			shuttingDown = true;
			try {
				executor.join();
			} catch (InterruptedException ie) {
				logger.severe(ie.getMessage());
			}
		}

		public int pushEvent(CIEventBase event) {
			return eventsList.add(event);
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
			if (conf != null && conf.location != null && conf.domain != null && conf.project != null && conf.username != null && conf.password != null) {
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
		if (!client.isActive()) client.activate();
	}

	public static void dispatchEvent(CIEventBase event) {
		for (Client c : clients) {
			if (c.isActive()) {
				c.pushEvent(event);
			}
		}
	}

	public static List<Client> getStatus() {
		return clients;
	}
}
