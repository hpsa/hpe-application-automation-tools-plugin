package com.hp.octane.plugins.jenkins.events;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/08/14
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */

@Extension
public final class ItemListenerImpl extends ItemListener {
	private static final Logger logger = LogManager.getLogger(ItemListenerImpl.class);

	public void onRenamed(Item item, String oldName, String newName) {
		logger.info("Renamed for: " + oldName + " => " + newName);
	}
}
