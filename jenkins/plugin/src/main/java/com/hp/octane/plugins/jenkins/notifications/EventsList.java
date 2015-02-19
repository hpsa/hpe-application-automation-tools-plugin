package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.actions.PluginActions;
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

	@Exported(inline = true)
	public PluginActions.ServerInfo getServer() {
		return new PluginActions.ServerInfo();
	}

	@Exported(inline = true)
	public List<CIEventBase> getEvents() {
		return events;
	}
}
