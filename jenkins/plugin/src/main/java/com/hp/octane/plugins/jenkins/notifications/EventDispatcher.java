package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.configuration.RestUtils;
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

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public final class EventDispatcher {

	@ExportedBean
	public static class Client {
		private final EventsList eventsList = new EventsList();
		private Thread executor;
		private boolean shuttingDown;
		private int failedRetries;

		@Exported(inline = true)
		public String url;
		@Exported(inline = true)
		public String domain;
		@Exported(inline = true)
		public String project;
		@Exported(inline = true)
		public String lastErrorNote;
		public Date lastErrorTime;

		@Exported(inline = true)
		public boolean isActive() {
			return executor != null && executor.isAlive();
		}

		public Client(String url, String domain, String project) {
			this.url = url;
			this.domain = domain;
			this.project = project;
		}

		public void activate() {
			eventsList.clear();
			shuttingDown = false;
			failedRetries = 0;
			executor = new Thread(new Runnable() {
				@Override
				public void run() {
					int status;
					int suspendTime = 3;
					List<CIEventBase> localList;
					while (!shuttingDown) {
						try {
							if (eventsList.size() > 0) {
								System.out.println("Pushing " + eventsList.size() + " event/s to '" + url + "'...");
								localList = new ArrayList<CIEventBase>(eventsList.getEvents());
								Writer w = new StringWriter();
								new ModelBuilder().get(EventsList.class).writeTo(eventsList, Flavor.JSON.createDataWriter(localList, w));
								status = RestUtils.put(url, buildUrl(), w.toString());
								if (status == 200) {
									eventsList.clear(localList);
									failedRetries = 0;
									suspendTime = 3;
								} else {
									lastErrorNote = "push to MQM server failed with status " + status;
									lastErrorTime = new Date();
									failedRetries++;
									System.out.println("Push to '" + url + "' failed with status '" + status + "'; total fails: " + failedRetries);
									if (failedRetries >= MAX_PUSH_RETRIES) {
										eventsList.clear();
										//	shuttingDown = true;
									} else {
										Thread.sleep(suspendTime * 1000);
										//	suspendTime *= 2;
									}
								}
								System.out.println("Done, " + eventsList.size() + " is/are in queue of '" + url + "'");
							} else {
								Thread.sleep(137);
							}
						} catch (Exception e) {
							lastErrorNote = "push to MQM server failed with exception '" + e.getMessage() + "'";
							lastErrorTime = new Date();
							failedRetries++;
							System.out.println("Push to '" + url + "' failed with exception '" + e.getMessage() + "'; total fails: " + failedRetries);
							if (failedRetries >= MAX_PUSH_RETRIES) {
								eventsList.clear();
								//	shuttingDown = true;
							}
						}
					}
					System.out.println("Events client for '" + url + "' shut down");
				}
			});
			executor.setDaemon(true);
			executor.start();
			System.out.println("New thread started for events client at '" + this.url + "'");
		}

		public void dispose() {
			shuttingDown = true;
			try {
				executor.join();
			} catch (InterruptedException ie) {
				System.out.println(ie.getMessage());
			}
		}

		public int pushEvent(CIEventBase event) {
			return eventsList.add(event);
		}

		private String buildUrl() {
			return "/qcbin/rest/domains/" + domain + "/projects/" + project + "/cia/events";
		}
	}

	private static final int MAX_PUSH_RETRIES = 3;
	private static final List<Client> clients = new ArrayList<Client>();

	public static void updateClient(String url, String domain, String project) {
		Client client = null;
		synchronized (clients) {
			for (Client c : clients) {
				if (c.url.equals(url)) {
					client = c;
				}
			}
			if (client == null) {
				client = new Client(url, domain, project);
				clients.add(client);
			}
		}
		if (!client.isActive()) client.activate();
	}

	public static void removeClient(Client client) {
		synchronized (clients) {
			client.dispose();
			clients.remove(client);
		}
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
