package com.hp.octane.integrations;

import com.hp.octane.integrations.api.CIPluginServices;
import com.hp.octane.integrations.api.RestService1;
import com.hp.octane.integrations.api.TasksProcessor;

/**
 * Created by gullery on 14/07/2016.
 * <p/>
 * Base class for SDK services, used to mediate the services and other internal data between the services and SDK
 */

public abstract class SDKService {
	private final OctaneSDK.SDKConfigurator configurator;

	protected SDKService(Object configurator) {
		if (configurator == null) {
			throw new IllegalArgumentException("configurator MUST NOT be null");
		}
		if (configurator instanceof OctaneSDK.SDKConfigurator) {
			this.configurator = (OctaneSDK.SDKConfigurator) configurator;
		} else {
			throw new IllegalArgumentException("configurator MUST be of a correct type");
		}
	}

	protected CIPluginServices getPluginServices() {
		return configurator.getPluginServices();
	}

	protected RestService1 getRestService() {
		return configurator.getRestService1();
	}

	protected TasksProcessor getTasksProcessor() {
		return configurator.getTasksProcessor();
	}
}
