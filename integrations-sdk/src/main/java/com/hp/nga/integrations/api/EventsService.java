package com.hp.nga.integrations.api;

import com.hp.nga.integrations.SDKServicePublic;
import com.hp.nga.integrations.dto.events.CIEventBase;

public interface EventsService extends SDKServicePublic {

	/**
	 * Publishes CI Event to the NGA server
	 *
	 * @param event
	 */
	void publishEvent(CIEventBase event);
}
