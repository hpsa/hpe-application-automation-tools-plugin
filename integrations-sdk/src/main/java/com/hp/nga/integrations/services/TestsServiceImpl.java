package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.TestsService;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.tests.TestResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 09/03/2016.
 */

class TestsServiceImpl implements TestsService {
	private static final Logger logger = LogManager.getLogger(TestsServiceImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private final SDKManager sdk;

	TestsServiceImpl(SDKManager sdk) {
		this.sdk = sdk;
	}

	public void pushTestsResult(TestResult testResult) {
		NGARestClient restClient = sdk.getInternalService(NGARestService.class).obtainClient();
	}

	public void enqueuePushTestsResult(String ciJobRefId, String ciBuildRefId) {
		//  TODO...
	}
}
