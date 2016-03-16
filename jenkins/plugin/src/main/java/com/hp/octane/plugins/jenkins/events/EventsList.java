package com.hp.octane.plugins.jenkins.events;

import com.hp.nga.integrations.dto.events.CIEventBase;
import com.hp.nga.integrations.dto.general.CIServerInfo;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import jenkins.model.Jenkins;
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
	public CIServerInfo getServer() {
		return Jenkins.getInstance().getPlugin(OctanePlugin.class).jenkinsPluginServices.getServerInfo();
	}

	@Exported(inline = true)
	public List<CIEventBase> getEvents() {
		return events;
	}
}
