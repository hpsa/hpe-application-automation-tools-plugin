package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.EventsService;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.events.CIEventBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 09/03/2016.
 * <p/>
 * Base implementation of Events Service API
 */

final class EventsServiceImpl implements EventsService {
	private static final Logger logger = LogManager.getLogger(EventsServiceImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	EventsServiceImpl() {
	}

	public void publishEvent(CIEventBase event) {
		//  TODO...
	}
}
