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

	public void clear() {
		events.clear();
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
