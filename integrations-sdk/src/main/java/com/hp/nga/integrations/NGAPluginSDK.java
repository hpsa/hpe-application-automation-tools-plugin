package com.hp.nga.integrations;

import com.hp.nga.integrations.api.CIPluginService;

/**
 * Created by gullery on 22/01/2016.
 * <p>
 * This class provides main entry point of interaction between an SDK and it's services and concrete plugin and it's services
 */

public class NGAPluginSDK {
	private CIPluginService ciPluginService;

	private NGAPluginSDK() {
	}

	public static NGAPluginSDK getInstance() {
		return InnerInstanceHolder.instance;
	}

	public CIPluginService getCiPluginService() {
		return ciPluginService;
	}

	public void setCiPluginService(CIPluginService ciPluginService) {
		this.ciPluginService = ciPluginService;
	}

	private static class InnerInstanceHolder {
		private static final NGAPluginSDK instance = new NGAPluginSDK();
	}
}
