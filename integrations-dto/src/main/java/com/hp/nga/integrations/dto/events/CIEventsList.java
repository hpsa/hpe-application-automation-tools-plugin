package com.hp.nga.integrations.dto.events;

import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.general.CIServerInfo;

import java.util.List;

/**
 * Created by gullery on 15/02/2015.
 * <p/>
 * CI Events list data object descriptor
 */

public interface CIEventsList extends DTOBase {

	CIServerInfo getServer();

	CIEventsList setServer(CIServerInfo server);

	List<CIEvent> getEvents();

	CIEventsList setEvents(List<CIEvent> events);
}
