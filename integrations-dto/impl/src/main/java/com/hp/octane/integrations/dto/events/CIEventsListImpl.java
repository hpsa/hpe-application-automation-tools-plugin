package com.hp.octane.integrations.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventsList;
import com.hp.octane.integrations.dto.general.CIServerInfo;

import java.util.List;

/**
 * Created by gullery on 15/02/2015.
 * <p/>
 * CI Events list data object descriptor
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CIEventsListImpl implements CIEventsList {
	private CIServerInfo server;
	private List<CIEvent> events;

	public CIServerInfo getServer() {
		return server;
	}

	public CIEventsList setServer(CIServerInfo server) {
		this.server = server;
		return this;
	}

	public List<CIEvent> getEvents() {
		return events;
	}

	public CIEventsList setEvents(List<CIEvent> events) {
		this.events = events;
		return this;
	}
}
