package com.hp.nga.api;

/**
 * Created by gullery on 07/01/2016.
 * <p/>
 * API definition of Plugin Info Service
 */

public abstract class PluginInfoServiceProvider {
	private static final Object INSTANCE_LOCK = new Object();
	private static volatile PluginInfoServiceProvider instance;

	public PluginInfoServiceProvider() {
		synchronized (INSTANCE_LOCK) {
			if (instance == null) {
				instance = this;
			} else {
				throw new RuntimeException("more than one instance of PluginInfoService not allowed");
			}
		}
	}

	public static PluginInfoServiceProvider getInstance() {
		if (instance != null) {
			return instance;
		} else {
			throw new RuntimeException("PluginInfoService MUST NOT be used prior to initialization");
		}
	}

	/**
	 * @return
	 */
	public abstract String getOwnUrl();

	/**
	 * @return
	 */
	public abstract String getInstanceId();
}
