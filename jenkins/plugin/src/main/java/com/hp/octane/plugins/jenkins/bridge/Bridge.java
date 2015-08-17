package com.hp.octane.plugins.jenkins.bridge;

import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import org.kohsuke.stapler.export.Exported;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by gullery on 12/08/2015.
 * <p/>
 * This class encompasses functionality managing connection to a single abridged client (server)
 */

public class Bridge {
	private static final Logger logger = Logger.getLogger(Bridge.class.getName());

	private final Object initLocker = new Object();
	private ExecutorService executors = Executors.newFixedThreadPool(5);
	private Thread worker;
	private boolean shuttingDown;

	private ServerConfiguration mqmConfig;
	private JenkinsMqmRestClientFactory restClientFactory;

	public Bridge(ServerConfiguration mqmConfig, JenkinsMqmRestClientFactory clientFactory) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		this.restClientFactory = clientFactory;
		connect();
		logger.info("BRIDGE: new bridge initialized for '" + this.mqmConfig.location + "'");
	}

	private void connect() {
		executors.execute(new Runnable() {
			@Override
			public void run() {
				try {
					//String taskBody = RESTClientTMP.get(mqmConfig.location + "/internal-api/shared_spaces/" + mqmConfig.sharedSpace + "/analytics/ci/task", null);
					//  parse the task, execute and post the result
					connect();
				} catch (Exception e) {
					//  TODO: handle the exception
					connect();
				}
			}
		});
	}

	public void update(ServerConfiguration mqmConfig) {
		this.mqmConfig = new ServerConfiguration(mqmConfig.location, mqmConfig.abridged, mqmConfig.sharedSpace, mqmConfig.username, mqmConfig.password);
		logger.info("BRIDGE: updated for '" + this.mqmConfig.location + "'");
	}

	@Exported(inline = true)
	public String getLocation() {
		return mqmConfig.location;
	}

	@Exported(inline = true)
	public String getSharedSpace() {
		return mqmConfig.sharedSpace;
	}

	@Exported(inline = true)
	public String getUsername() {
		return mqmConfig.username;
	}
}
