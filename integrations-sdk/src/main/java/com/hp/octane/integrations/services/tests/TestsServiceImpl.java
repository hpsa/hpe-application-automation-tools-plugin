package com.hp.octane.integrations.services.tests;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.SDKService;
import com.hp.octane.integrations.api.RestClient;
import com.hp.octane.integrations.api.TestsService;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.connectivity.HttpMethod;
import com.hp.octane.integrations.dto.connectivity.OctaneRequest;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.hp.octane.integrations.dto.tests.TestsResult;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Created by gullery on 09/03/2016.
 * <p/>
 * Default implementation of tests service
 */

public final class TestsServiceImpl extends SDKService implements TestsService {
	private static final Logger logger = LogManager.getLogger(TestsServiceImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private final Object INIT_LOCKER = new Object();

	private static List<BuildNode> buildList = Collections.synchronizedList(new LinkedList<BuildNode>());
	private int DATA_SEND_INTERVAL = 60000;
	private int LIST_EMPTY_INTERVAL = 3000;
	private Thread worker;

	public TestsServiceImpl(Object configurator) {
		super(configurator);
		activate();
	}

	public OctaneResponse pushTestsResult(TestsResult testsResult) throws IOException {
		if (testsResult == null) {
			throw new IllegalArgumentException("tests result MUST NOT be null");
		}

		RestClient restClientImpl = getRestService().obtainClient();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/xml");
		OctaneRequest request = dtoFactory.newDTO(OctaneRequest.class)
				.setMethod(HttpMethod.POST)
				.setUrl(getPluginServices().getOctaneConfiguration().getUrl() + "/internal-api/shared_spaces/" +
						getPluginServices().getOctaneConfiguration().getSharedSpace() + "/analytics/ci/test-results?skip-errors=false")
				.setHeaders(headers)
				.setBody(dtoFactory.dtoToXml(testsResult));
		OctaneResponse response = restClientImpl.execute(request);
		logger.info("tests result pushed with " + response);
		return response;
	}

	public void enqueuePushTestsResult(String jobId, String buildNumber) {
		buildList.add(new BuildNode(jobId, buildNumber));
	}

	private void activate() {
		if (worker == null || !worker.isAlive()) {
			synchronized (INIT_LOCKER) {
				if (worker == null || !worker.isAlive()) {
					worker = new Thread(new Runnable() {
						public void run() {
							while (true) {
								if (!buildList.isEmpty()) {
									try {
										BuildNode buildNode = buildList.get(0);
										TestsResult testsResult = getPluginServices().getTestsResult(buildNode.jobId, buildNode.buildNumber);
										OctaneResponse response = pushTestsResult(testsResult);
										if (response.getStatus() == HttpStatus.SC_ACCEPTED) {
											logger.info("Push tests result was successful");
											buildList.remove(0);
										} else if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
											logger.info("tests result push failed because of service unavailable; retrying");
											breathe(DATA_SEND_INTERVAL);
										} else {
											//  case of any other fatal error
											logger.error("failed to submit tests result with " + response.getStatus() + "; dropping this item from the queue");
											buildList.remove(0);
										}
									} catch (IOException e) {
										logger.error("Tests result push IOException; retrying", e);
										breathe(DATA_SEND_INTERVAL);
									}
								} else {
									breathe(LIST_EMPTY_INTERVAL);
								}
							}
						}
					});
					worker.setDaemon(true);
					worker.setName("TestPushWorker");
					worker.start();
				}
			}
		}
	}

	//  TODO: turn to be breakable wait with timeout and notifier
	private void breathe(int period) {
		try {
			Thread.sleep(period);
		} catch (InterruptedException ie) {
			logger.error("interrupted while breathing", ie);
		}
	}

	private static final class BuildNode {
		private final String jobId;
		private final String buildNumber;

		private BuildNode(String jobId, String buildNumber) {
			this.jobId = jobId;
			this.buildNumber = buildNumber;
		}
	}
}
