package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.configuration.RestUtils;
import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.ModelBuilder;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public final class EventDispatcher {
	static class Client {
		private final EventsList eventsList = new EventsList();
		private Thread executor;
		private boolean shuttingDown;
		public String clientURL;

		public Client(String cURL) {
			this.clientURL = cURL;
			shuttingDown = false;
			executor = new Thread(new Runnable() {
				@Override
				public void run() {
					int status;
					List<CIEventBase> localList;
					while (!shuttingDown) {
						try {
							if (eventsList.getEvents().size() > 0) {
								localList = new ArrayList<CIEventBase>(eventsList.getEvents());
								Writer w = new StringWriter();
								new ModelBuilder().get(EventsList.class).writeTo(eventsList, Flavor.JSON.createDataWriter(localList, w));
								status = RestUtils.post(clientURL + "", w.toString());
								if (status == 201) {
									eventsList.getEvents().removeAll(localList);
								}
							}
							Thread.sleep(100);
						} catch (Exception e) {
							e.printStackTrace();
							//  TODO: decide what to do on the exception case
						}
					}
				}
			});
			executor.start();
			System.out.println("New thread started to push events to '" + this.clientURL + "'");
		}

		public void dispose() {
			shuttingDown = true;
		}

		public int pushEvent(CIEventBase event) {
			synchronized (eventsList) {
				eventsList.getEvents().add(event);
			}
			return eventsList.getEvents().size();
		}
	}

	public static final String SELF_URL;
	private static final List<Client> clients = new ArrayList<Client>();

	static {
		String selfUrl = Jenkins.getInstance().getRootUrl();
		if (selfUrl != null && selfUrl.endsWith("/")) selfUrl = selfUrl.substring(0, selfUrl.length() - 1);
		SELF_URL = selfUrl;

		//  TODO: the below code should be moved or refactored according to the configurational core by ALI
		//  TODO: part of the below URL should be retrieved from the integration user data
		addClient("http://localhost:8080/qcbin/rest/domains/DEFAULT/projects/CIA_P1/cia/events");
	}

	public static Client addClient(String clientURL) {
		Client client = new Client(clientURL);
		clients.add(client);
		return client;
	}

	public static void removeClient(Client client) {
		clients.remove(client);
		client.dispose();
	}

	public static void dispatchEvent(CIEventBase event) {
		for (Client c : clients) {
			c.pushEvent(event);
		}
	}
}
