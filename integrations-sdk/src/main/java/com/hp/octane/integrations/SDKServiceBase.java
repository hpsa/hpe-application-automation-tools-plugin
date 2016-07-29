package com.hp.octane.integrations;

import com.hp.octane.integrations.spi.CIPluginServices;
import com.hp.octane.integrations.api.RestService;
import com.hp.octane.integrations.api.TasksProcessor;

/**
 * Created by gullery on 14/07/2016.
 * <p/>
 * Base class for SDK services, used to mediate the services and other internal data between the services and SDK
 */

public abstract class SDKServiceBase {
	private final OctaneSDK.SDKConfigurator configurator;

	protected SDKServiceBase(Object configurator) {
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

	protected RestService getRestService() {
		return configurator.getRestService();
	}

	protected TasksProcessor getTasksProcessor() {
		return configurator.getTasksProcessor();
	}
}
