package com.hp.nga.integrations.services.rest;

import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;

/**
 * Created by gullery on 14/01/2016.
 * <p>
 * REST Client wrapper aligned and targeted to NGA use-cases
 */

public interface NGARestClient {
	NGAResponse execute(NGARequest request);
}
