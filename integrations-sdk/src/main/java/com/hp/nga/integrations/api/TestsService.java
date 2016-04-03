package com.hp.nga.integrations.api;

import com.hp.nga.integrations.SDKServicePublic;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import com.hp.nga.integrations.dto.tests.TestsResult;

import java.io.IOException;

public interface TestsService extends SDKServicePublic {

	/**
	 * Publishes CI Event to the NGA server
	 * Tests result pushed to NGA in a synchronous manner, use this method with caution
	 * Returns the NGAResponse.
	 * throws IOException
	 *
	 * @param testsResult
	 */
	NGAResponse pushTestsResult(TestsResult testsResult) throws IOException;

	/**
	 * Enqueue push tests result by submitting build reference for future tests retrieval
	 * This is the preferred way to push tests results to NGA, since provides facilities of queue, non-main thread execution and retry
	 *
	 * @param jobId
	 * @param buildNumber
	 */
	void enqueuePushTestsResult(String jobId, String buildNumber);
}
