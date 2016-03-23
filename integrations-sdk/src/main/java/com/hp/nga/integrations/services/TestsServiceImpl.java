package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.TestsService;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;
import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import com.hp.nga.integrations.dto.tests.TestResult;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by gullery on 09/03/2016.
 */

class TestsServiceImpl implements TestsService {
	private static final Logger logger = LogManager.getLogger(TestsServiceImpl.class);

	private int DATA_SEND_INTERVAL = 60000;
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private final SDKManager sdk;
	private static BlockingQueue<BuildNode> blockingQueue = new LinkedBlockingDeque<BuildNode>(Integer.MAX_VALUE);
	private final Object INIT_LOCKER = new Object();
	private static final int IOEXCEPTION_CODE = -1;
	private Thread worker;


	TestsServiceImpl(SDKManager sdk) {
		this.sdk = sdk;
		activate();
	}

	public int pushTestsResult(TestResult testResult) {
		NGARestClient restClient = sdk.getInternalService(NGARestService.class).obtainClient();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/xml");
		NGARequest request = dtoFactory.newDTO(NGARequest.class)
				.setMethod(NGAHttpMethod.POST)
				.setUrl(sdk.getCIPluginServices().getNGAConfiguration().getUrl() + "/api/shared_spaces/" +
						sdk.getCIPluginServices().getNGAConfiguration().getSharedSpace() + "/workspaces/1002/test-results?skip-errors=false")
				.setHeaders(headers)
				.setBody(dtoFactory.dtoToXml(testResult));
		try {
			NGAResponse response = restClient.execute(request);
			logger.info("tests result pushed with message " + response);
			return response.getStatus();
		} catch (IOException ioe) {
			logger.error("failed to push tests result", ioe);
			return IOEXCEPTION_CODE;
		}
	}

	public void enqueuePushTestsResult(String ciJobRefId, String ciBuildRefId) {
		blockingQueue.add(new BuildNode(ciJobRefId, ciBuildRefId));
	}

	private void activate() {
		if (worker == null || !worker.isAlive()) {
			synchronized (INIT_LOCKER) {
				if (worker == null || !worker.isAlive()) {
					worker = new Thread(new Runnable() {

						public void run() {
							while (true) {
								try {
									BuildNode buildNode = blockingQueue.take();
									TestResult testResult = sdk.getCIPluginServices().getTestResults(buildNode.getCiJobRefId(), buildNode.getCiBuildRefId());
									if(testResult != null){
										int status = pushTestsResult(testResult);
										if (status == HttpStatus.SC_SERVICE_UNAVAILABLE || status == IOEXCEPTION_CODE) {
											blockingQueue.add(buildNode);
											logger.info("tests result push failed because of service unavailable. retry pushing ");
											Thread.sleep(DATA_SEND_INTERVAL);
										}
									}
								} catch (Exception e) {
									logger.error("TEST PUSH: failed to send test results", e);
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



	class BuildNode {
		String ciJobRefId;
		String ciBuildRefId;


		BuildNode(String ciJobRefId, String ciBuildRefId) {
			this.ciJobRefId = ciJobRefId;
			this.ciBuildRefId = ciBuildRefId;
		}

		String getCiJobRefId() {
			return ciJobRefId;
		}

		void setCiJobRefId(String ciJobRefId) {
			this.ciJobRefId = ciJobRefId;
		}

		String getCiBuildRefId() {
			return ciBuildRefId;
		}

		void setCiBuildRefId(String ciBuildRefId) {
			this.ciBuildRefId = ciBuildRefId;
		}
	}
}
