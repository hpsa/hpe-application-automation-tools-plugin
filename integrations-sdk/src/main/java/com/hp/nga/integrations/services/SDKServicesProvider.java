package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;

/**
 * Created by gullery on 09/03/2016.
 * <p/>
 * Service provider, instantiates and collects services of an SDK
 * TODO: consider to break the services back to packages and have provider like this in each one of them
 */

public class SDKServicesProvider {

	public void registerServices(SDKManager manager, boolean startBridge) {
		manager.publicList.add(new ConfigurationServiceImpl(manager));
		manager.publicList.add(new EventsServiceImpl(manager));
		manager.publicList.add(new TasksProcessorImpl(manager));
		manager.publicList.add(new TestsServiceImpl(manager));

		manager.internalList.add(new LoggingService(manager));
		manager.internalList.add(new OctaneRestService(manager));
		manager.internalList.add(new BridgeService(manager, startBridge));
	}
}
