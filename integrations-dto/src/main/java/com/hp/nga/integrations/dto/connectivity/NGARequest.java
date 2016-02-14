package com.hp.nga.integrations.dto.connectivity;

import com.hp.nga.integrations.dto.DTOBase;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 * <p>
 * REST Request descriptor (normalized to NGA usages)
 */

public interface NGARequest extends DTOBase {

	String getUrl();

	NGARequest setUrl(String url);

	NGAHttpMethod getMethod();

	NGARequest setMethod(NGAHttpMethod method);

	Map<String, String> getHeaders();

	NGARequest setHeaders(Map<String, String> headers);

	String getBody();

	NGARequest setBody(String body);
}
