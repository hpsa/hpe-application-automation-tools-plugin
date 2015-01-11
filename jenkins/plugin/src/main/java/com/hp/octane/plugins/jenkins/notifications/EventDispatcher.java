package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import jenkins.model.Jenkins;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 31/08/14
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public final class EventDispatcher {
	static class Client {
		public String clientUri;
		public int connectFails;
		public boolean suspended;
		public final int connectFailsTolerance = 3;

		public Client(String cUri) {
			this.clientUri = cUri;
		}
	}

	public static final String SELF_URL;
	private static final ArrayList<Client> clients = new ArrayList<Client>();

	//  TODO:
	//  persist the subscribers' list by the means Jenkins provides

	static {
		String selfUrl = Jenkins.getInstance().getRootUrl();
		if (selfUrl.endsWith("/")) selfUrl = selfUrl.substring(0, selfUrl.length() - 1);
		SELF_URL = selfUrl;
	}

	public static synchronized void updateClient(String clientUri) {
		Client tmp = null;
		for (Client c : clients) {
			if (c.clientUri.equals(clientUri)) {
				tmp = c;
				break;
			}
		}
		if (tmp == null) {
			tmp = new Client(clientUri);
			clients.add(tmp);
		}
		tmp.connectFails = 0;
		tmp.suspended = false;
	}

	public static void dispatchEvent(CIEventBase event) {
		DefaultHttpClient client;
		HttpPost request;
		HttpResponse response;
//		JSONObject tmp;
		StringEntity entity;

		for (Client c : clients) {
			if (c.suspended) continue;

			try {
				System.out.println("Pushing event '" + event.getEventType() + "' to " + c.clientUri);
//				tmp = event.toJSON();
				entity = new StringEntity("{}");    //  removed here the serialization of event
				request = new HttpPost(c.clientUri + "/rest/realtime/notification");
				//request = new HttpPost("http://localhost:8889" + "/rest/realtime/notification");
				request.setEntity(entity);
				client = new DefaultHttpClient();
				response = client.execute(request);
				c.connectFails = 0;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				c.connectFails++;
				if (c.connectFails == c.connectFailsTolerance) c.suspended = true;
			}
		}
	}
}
