package com.hp.nga.integrations.services.rest;

import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;

/**
 * Created by gullery on 14/01/2016.
 * <p>
 * REST Service providing a centralized access to NGA REST client/s and any additional common functionalities
 */

public interface NGARestService {
	NGARestClient obtainClient();

	NGAResponse testConnection(NGAConfiguration configuration);
}
