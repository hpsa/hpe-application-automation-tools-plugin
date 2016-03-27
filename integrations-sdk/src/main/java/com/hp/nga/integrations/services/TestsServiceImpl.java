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
import java.util.*;

/**
 * Created by gullery on 09/03/2016.
 *
 * Default implementation of tests service
 */

class TestsServiceImpl implements TestsService {
    private static final Logger logger = LogManager.getLogger(TestsServiceImpl.class);

    private int DATA_SEND_INTERVAL = 60000;
    private int LIST_EMPTY_INTERVAL = 3000;
    private static final DTOFactory dtoFactory = DTOFactory.getInstance();
    private final SDKManager sdk;
    private static List<BuildNode> buildList = Collections.synchronizedList(new LinkedList<BuildNode>());
    private final Object INIT_LOCKER = new Object();
    private Thread worker;
	
    TestsServiceImpl(SDKManager sdk) {
        this.sdk = sdk;
        activate();
    }

	public NGAResponse pushTestsResult(TestResult testResult) throws IOException {
		NGARestClient restClient = sdk.getInternalService(NGARestService.class).obtainClient();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("content-type", "application/xml");
		NGARequest request = dtoFactory.newDTO(NGARequest.class)
				.setMethod(NGAHttpMethod.POST)
				.setUrl(sdk.getCIPluginServices().getNGAConfiguration().getUrl() + "/internal-api/shared_spaces/" +
						sdk.getCIPluginServices().getNGAConfiguration().getSharedSpace() + "/analytics/ci/test-results?skip-errors=false")
				.setHeaders(headers)
				.setBody(dtoFactory.dtoToXml(testResult));
			NGAResponse response = restClient.execute(request);
			logger.info("tests result pushed with " + response);
			return response;
	}

    public void enqueuePushTestsResult(String ciJobRefId, String ciBuildRefId) {
        buildList.add(new BuildNode(ciJobRefId, ciBuildRefId));
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
                                        TestResult testResult = sdk.getCIPluginServices().getTestResults(buildNode.getCiJobRefId(), buildNode.getCiBuildRefId());
                                        if (testResult != null) {
                                            NGAResponse response = pushTestsResult(testResult);
                                            if (response.getStatus() == HttpStatus.SC_ACCEPTED) {
                                                logger.info("Push test result was successful ");
                                                buildList.remove(0);
                                            } else if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                                                logger.info("tests result push failed because of service unavailable. retry pushing ");
                                                Thread.sleep(DATA_SEND_INTERVAL);
                                            }
                                        }
                                    } catch (InterruptedException e) {
                                        logger.error("Test Push Result - InterruptedException while sleeping", e);
                                    } catch (IOException e) {
                                        logger.error("Test Push IOException - retry pushing", e);
                                        try {
                                            Thread.sleep(DATA_SEND_INTERVAL);
                                        } catch (InterruptedException e1) {
                                            logger.error("Test Push Result - InterruptedException while sleeping after IOException ", e);
                                        }
                                    }
                                } else {
                                    try {
                                        Thread.sleep(LIST_EMPTY_INTERVAL);
                                    } catch (InterruptedException ie) {
                                        logger.error("Test Push Result - InterruptedException while waiting for list to get builds", ie);
                                    }
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
