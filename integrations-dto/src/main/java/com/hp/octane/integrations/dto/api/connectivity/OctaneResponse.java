package com.hp.octane.integrations.dto.api.connectivity;

import com.hp.octane.integrations.dto.DTOBase;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 * <p>
 * REST Response descriptor (normalized to NGA usages)
 */

public interface OctaneResponse extends DTOBase {

	int getStatus();

	OctaneResponse setStatus(int status);

	Map<String, String> getHeaders();

	OctaneResponse setHeaders(Map<String, String> headers);

	String getBody();

	OctaneResponse setBody(String body);
}
