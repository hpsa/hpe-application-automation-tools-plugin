package com.hp.nga.integrations.services.rest;

import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;

import java.io.IOException;

/**
 * Created by gullery on 14/01/2016.
 * <p/>
 * REST Client wrapper aligned and targeted to NGA use-cases
 */

public interface NGARestClient {

	/**
	 * Executes HTTP request in context of NGA specific functionality
	 *
	 * @param request
	 * @return response; an implementation MUST NOT return null
	 * @throws IOException
	 */
	NGAResponse execute(NGARequest request) throws RuntimeException;
}
