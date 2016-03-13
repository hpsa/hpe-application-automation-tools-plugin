package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.TestsService;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;
import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import com.hp.nga.integrations.dto.tests.TestResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
			System.out.println(response);
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	public void enqueuePushTestsResult(String ciJobRefId, String ciBuildRefId) {
		//  TODO...
	}
}
