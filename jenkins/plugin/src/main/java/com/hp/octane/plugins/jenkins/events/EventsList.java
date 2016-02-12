package com.hp.octane.plugins.jenkins.events;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.events.CIEventBase;
import com.hp.nga.integrations.dto.general.ServerInfo;
import com.hp.octane.plugins.jenkins.actions.PluginActions;
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
	public ServerInfo getServer() {
		ServerInfo serverInfo = DTOFactory.getInstance().newDTO(ServerInfo.class);
		//  currently not inflating nothing
		return serverInfo;
	}

	@Exported(inline = true)
	public List<CIEventBase> getEvents() {
		return events;
	}
}
