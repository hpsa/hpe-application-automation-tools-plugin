package com.hp.nga.integrations.dto.connectivity;

import com.hp.nga.integrations.dto.DTOBase;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 */

public interface NGAResponse extends DTOBase {

	int getStatus();

	NGAResponse setStatus(int status);

	Map<String, String> getHeaders();

	NGAResponse setHeaders(Map<String, String> headers);

	String getBody();

	NGAResponse setBody(String body);
}
