package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.EventsService;
import com.hp.nga.integrations.dto.events.CIEventBase;

public class EventsServiceImpl implements EventsService {

	public void submitEvent(CIEventBase event) {
		//  meta code for example
		//  put event to queue
		//  on another thread
		//      get from queue
		//      get http client
		//      push event to NGA
	}
}
