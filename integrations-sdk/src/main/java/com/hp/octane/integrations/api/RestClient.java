package com.hp.octane.integrations.api;

import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.connectivity.OctaneRequest;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;

import java.io.IOException;

public interface RestClient {

	/**
	 * Executes Octane server oriented request based on the pre-configuration
	 *
	 * @param request
	 * @return
	 * @throws IOException
	 */
	OctaneResponse execute(OctaneRequest request) throws IOException;

	/**
	 * Executes Octane server oriented request based on the provided configuration
	 *
	 * @param request
	 * @param configuration
	 * @return
	 * @throws IOException
	 */
	OctaneResponse execute(OctaneRequest request, OctaneConfiguration configuration) throws IOException;
}
