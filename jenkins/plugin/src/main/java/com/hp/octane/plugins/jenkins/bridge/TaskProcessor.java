package com.hp.octane.plugins.jenkins.bridge;

import java.util.logging.Logger;

/**
 * Created by gullery on 17/08/2015.
 * <p/>
 * This class is a Tasks Processor definition to be run in separate thread
 */

public class TaskProcessor implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskProcessor.class.getName());

	TaskProcessor(String taskJSON) {
		logger.info("processing task " + taskJSON);
	}

	@Override
	public void run() {

	}
}
