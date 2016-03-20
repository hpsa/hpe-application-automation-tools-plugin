package com.hp.nga.integrations.api;

import com.hp.nga.integrations.SDKServicePublic;
import com.hp.nga.integrations.dto.tests.TestResult;

public interface TestsService extends SDKServicePublic {

	/**
	 * Publishes CI Event to the NGA server
	 * Tests result pushed to NGA in a synchronous manner, use this method with caution
	 *
	 * @param testResult
	 */
	void pushTestsResult(TestResult testResult);

	/**
	 * Enqueue push tests result by submitting build reference for future tests retrieval
	 * This is the preferred way to push tests results to NGA, since provides facilities of queue, non-main thread execution and retry
	 *
	 * @param ciJobRefId
	 * @param ciBuildRefId
	 */
	void enqueuePushTestsResult(String ciJobRefId, String ciBuildRefId);
}
