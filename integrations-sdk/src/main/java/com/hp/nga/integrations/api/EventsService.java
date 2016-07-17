package com.hp.nga.integrations.api;

import com.hp.nga.integrations.SDKServicePublic;
import com.hp.octane.integrations.dto.events.CIEvent;

public interface EventsService extends SDKServicePublic {

	/**
	 * Publishes CI Event to the NGA server
	 *
	 * @param event
	 */
	void publishEvent(CIEvent event);
}
