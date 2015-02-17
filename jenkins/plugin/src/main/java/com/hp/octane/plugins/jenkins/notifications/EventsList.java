package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 15/02/2015.
 */
@ExportedBean
public class EventsList {
	private final List<CIEventBase> events = new ArrayList<CIEventBase>();
	private String serverURL;

	public EventsList(String serverURL) {
		this.serverURL = serverURL;
	}

	public int add(CIEventBase event) {
		synchronized (events) {
			events.add(event);
		}
		return events.size();
	}

	public void clear() {
		synchronized (events) {
			events.clear();
		}
	}

	public void clear(List<CIEventBase> sent) {
		synchronized (events) {
			events.removeAll(sent);
		}
	}

	public int size() {
		return events.size();
	}

	@Exported
	public String serverType() {
		return "jenkins";
	}

	@Exported
	public String serverURL() {
		return serverURL;
	}

	@Exported(inline = true)
	public List<CIEventBase> getEvents() {
		return events;
	}
}
