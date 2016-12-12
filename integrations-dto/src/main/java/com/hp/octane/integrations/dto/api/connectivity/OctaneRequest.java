package com.hp.octane.integrations.dto.api.connectivity;

import com.hp.octane.integrations.dto.DTOBase;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 * <p>
 * REST Request descriptor (normalized to NGA usages)
 */

public interface OctaneRequest extends DTOBase {

	String getUrl();

	OctaneRequest setUrl(String url);

	HttpMethod getMethod();

	OctaneRequest setMethod(HttpMethod method);

	Map<String, String> getHeaders();

	OctaneRequest setHeaders(Map<String, String> headers);

	String getBody();

	OctaneRequest setBody(String body);
}
