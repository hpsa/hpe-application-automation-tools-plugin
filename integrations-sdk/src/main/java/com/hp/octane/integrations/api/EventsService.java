package com.hp.octane.integrations.api;

import com.hp.octane.integrations.dto.events.CIEvent;

public interface EventsService {

	/**
	 * Publishes CI Event to the NGA server
	 *
	 * @param event
	 */
	void publishEvent(CIEvent event);
}
