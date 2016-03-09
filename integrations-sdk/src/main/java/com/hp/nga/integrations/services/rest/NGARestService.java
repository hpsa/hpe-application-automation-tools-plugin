package com.hp.nga.integrations.services.rest;

import com.hp.nga.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;

import java.io.IOException;
import java.net.ConnectException;

/**
 * Created by gullery on 14/01/2016.
 * <p/>
 * REST Service providing a centralized access to NGA REST client/s and any additional common functionalities
 */

public interface NGARestService {

	/**
	 * Returns default HTTP Client. Unless anything changed in the configuration the same client will be supplied for any consumer
	 *
	 * @return
	 */
	NGARestClient obtainClient();

	/**
	 * Tests connectivity to the NGA server with the supplied configuration
	 *
	 * @param configuration
	 * @return
	 * @throws RuntimeException in case of connection failure
	 */
	NGAResponse testConnection(NGAConfiguration configuration) throws RuntimeException;
}
