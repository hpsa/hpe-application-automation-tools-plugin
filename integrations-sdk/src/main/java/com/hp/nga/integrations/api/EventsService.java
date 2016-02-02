package com.hp.nga.integrations.api;

import com.hp.nga.integrations.dto.events.CIEventBase;

public interface EventsService {
	void submitEvent(CIEventBase event);
}
