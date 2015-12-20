package com.hp.octane.plugins.jenkins.events;

import com.hp.octane.plugins.jenkins.actions.PluginActions;
import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gullery on 15/02/2015.
 */

@ExportedBean
public class EventsList {
	private List<CIEventBase> events;

	public EventsList(List<CIEventBase> events) {
		if (events == null) {
			throw new IllegalArgumentException("EVENTS: events list MUST NOT be null");
		}
		this.events = Collections.unmodifiableList(new ArrayList<CIEventBase>(events));
	}

	@Exported(inline = true)
	public PluginActions.ServerInfo getServer() {
		return new PluginActions.ServerInfo();
	}

	@Exported(inline = true)
	public List<CIEventBase> getEvents() {
		return events;
	}
}
